package org.radarbase.auth.kratos_session

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.radarbase.auth.authentication.TokenVerifier
import org.radarbase.auth.authorization.AuthorityReference
import org.radarbase.auth.authorization.RoleAuthority
import org.radarbase.auth.exception.TokenValidationException
import org.radarbase.auth.token.DataRadarToken
import org.radarbase.auth.token.RadarToken
import org.slf4j.LoggerFactory
import java.time.Duration
import java.time.Instant

class KratosTokenVerifier : TokenVerifier {
    private val httpClient = HttpClient(CIO).config {
        install(HttpTimeout) {
            connectTimeoutMillis = Duration.ofSeconds(10).toMillis()
            socketTimeoutMillis = Duration.ofSeconds(10).toMillis()
            requestTimeoutMillis = Duration.ofSeconds(300).toMillis()
        }
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
            })
        }
    }

    val kratosBaseUrl = ("http://localhost:" + "4434")

    override fun verify(token: String): RadarToken = try {
        val cookie = "ory_kratos_session=" + token.subSequence(7, token.length)

        var kratosSession: KratosDTO? = null

        runBlocking {
            val response = httpClient.get{
                header("Cookie", cookie)
                url("$kratosBaseUrl/sessions/whoami")
                accept(ContentType.Application.Json)
            }

            if (response.status.isSuccess()) {
                kratosSession = response.body<KratosDTO>();
            }
            else {
                throw TokenValidationException("couldn't get kratos session");
            }
        }


        if (kratosSession != null) {
            DataRadarToken(
                roles = kratosSession!!.identity?.metadata_public?.parseRoles()!!,
                scopes = kratosSession!!.identity?.metadata_public?.scope?.toSet()!!,
                sources = kratosSession!!.identity?.metadata_public?.sources!!,
                grantType = "session",
                subject = kratosSession!!.identity?.id,
                issuedAt = Instant.parse(kratosSession!!.issued_at),
                expiresAt = Instant.parse(kratosSession!!.expires_at),
                audience = kratosSession!!.identity?.metadata_public?.aud!!,
                token = token,
                issuer = kratosSession!!.authentication_methods?.first()?.provider,
                type = "type",
                clientId = "kratosSession",
                username = kratosSession!!.identity?.metadata_public?.mp_login
            )
        }
        else {
            throw TokenValidationException("couldn't get kratos session");
        }
    } catch (ex: Throwable) {
        throw ex
    }

    override fun toString(): String = "KratosTokenVerifier"

    companion object {
        private val logger = LoggerFactory.getLogger(KratosTokenVerifier::class.java)

        private fun Metadata.parseRoles(): Set<AuthorityReference> = buildSet {
            if (!this@parseRoles.authorities.isNullOrEmpty()) {
                for (roleValue in this@parseRoles.authorities!!) {
                    val authority = RoleAuthority.valueOfAuthorityOrNull(roleValue)
                    if (authority?.scope == RoleAuthority.Scope.GLOBAL) {
                        add(AuthorityReference(authority))
                    }
                }
            }
            if (!this@parseRoles.roles.isNullOrEmpty()) {
                for (roleValue in this@parseRoles.roles!!) {
                    val role = RoleAuthority.valueOfAuthorityOrNull(roleValue)
                    if (role?.scope == RoleAuthority.Scope.GLOBAL) {
                        add(AuthorityReference(role))
                    }
                }
            }
        }
    }

    @Serializable
    class AuthenticationMethod {
        var method: String? = null
        var aal: String? = null
        var completed_at: String? = null
        var provider: String? = null
    }

    @Serializable
    class Device {
        var id: String? = null
        var ip_address: String? = null
        var user_agent: String? = null
        var location: String? = null
    }

    @Serializable
    class Identity {
        var id: String? = null
        var schema_id: String? = null
        var schema_url: String? = null
        var state: String? = null
        var state_changed_at: String? = null
        var traits: Traits? = null
        var metadata_public: Metadata? = null
        var created_at: String? = null
        var updated_at: String? = null
    }


    @Serializable
    class KratosDTO {
        var id: String? = null
        var active = false
        var expires_at: String? = null
        var authenticated_at: String? = null
        var authenticator_assurance_level: String? = null
        var authentication_methods: ArrayList<AuthenticationMethod>? = null
        var issued_at: String? = null
        var identity: Identity? = null
        var devices: ArrayList<Device>? = null
    }


    @Serializable
    class Traits {
        var name: String? = null
        var email: String? = null
        var mp_id = 0
    }

    @Serializable
    class Metadata {
        var roles: ArrayList<String>? = null
        var authorities: ArrayList<String>? = null
        var scope: ArrayList<String>? = null
        var sources: ArrayList<String>? = null
        var aud: ArrayList<String>? = null
        var mp_login: String? = ""
    }
}
