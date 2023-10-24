import kotlinx.serialization.encoding.Decoder
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
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
import java.util.*
import kotlin.collections.ArrayList

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

    override suspend fun verify(token: String): RadarToken = try {
        val cookie = "ory_kratos_session=" + token

        val kratosSession: KratosDTO

        withContext(Dispatchers.IO) {
            val response = httpClient.get {
                header("Cookie", cookie)
                url("$kratosBaseUrl/sessions/whoami")
                accept(ContentType.Application.Json)
            }

            if (response.status.isSuccess()) {
                logger.debug(response.body())
                kratosSession = response.body<KratosDTO>()
            } else {
                throw TokenValidationException("couldn't get kratos session")
            }
        }

        DataRadarToken(
            roles = kratosSession.identity.metadata_public.parseRoles(),
            scopes = kratosSession.identity.metadata_public.scope.toSet(),
            sources = kratosSession.identity.metadata_public.sources,
            grantType = "session",
            subject = kratosSession.identity.id,
            issuedAt = kratosSession.issued_at,
            expiresAt = kratosSession.expires_at,
            audience = kratosSession.identity.metadata_public.aud,
            token = token,
            issuer = kratosSession.authentication_methods.first().provider,
            type = "type",
            clientId = "kratosSession",
            username = kratosSession.identity.metadata_public.mp_login
        )
    } catch (ex: Throwable) {
        throw ex
    }

    object InstantSerializer : KSerializer<Instant> {
        override val descriptor = PrimitiveSerialDescriptor("Instant", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): Instant {
            return Instant.parse(decoder.decodeString())
        }

        override fun serialize(encoder: kotlinx.serialization.encoding.Encoder, value: Instant) {
            encoder.encodeString(value.toString())
        }
    }
    override fun toString(): String = "KratosTokenVerifier"

    companion object {
        private val logger = LoggerFactory.getLogger(KratosTokenVerifier::class.java)

        private fun Metadata.parseRoles(): Set<AuthorityReference> = buildSet {
            if (this@parseRoles.authorities.isNotEmpty()) {
                for (roleValue in this@parseRoles.authorities) {
                    val authority = RoleAuthority.valueOfAuthorityOrNull(roleValue)
                    if (authority?.scope == RoleAuthority.Scope.GLOBAL) {
                        add(AuthorityReference(authority))
                    }
                }
            }
            if (this@parseRoles.roles.isNotEmpty()) {
                for (roleValue in this@parseRoles.roles) {
                    val role = RoleAuthority.valueOfAuthorityOrNull(roleValue)
                    if (role?.scope == RoleAuthority.Scope.GLOBAL) {
                        add(AuthorityReference(role))
                    }
                }
            }
        }
    }

    @Serializable
    data class AuthenticationMethod(
        val method: String,
        val aal: String,
        @Serializable(with = InstantSerializer::class)
        val completed_at: Instant,
        val provider: String
    )

    @Serializable
    class Device(
        val id: String,
        val ip_address: String,
        val user_agent: String,
        val location: String,
    )

    @Serializable
    class Identity (
        val id: String,
        val schema_id: String,
        val schema_url: String,
        val state: String,
        @Serializable(with = InstantSerializer::class)
        val state_changed_at: Instant,
        val traits: Traits?,
        val metadata_public: Metadata,
        @Serializable(with = InstantSerializer::class)
        val created_at: Instant,
        @Serializable(with = InstantSerializer::class)
        val updated_at: Instant,
    )


    @Serializable
    class KratosDTO (
        val id: String,
        val active: Boolean,
        @Serializable(with = InstantSerializer::class)
        val expires_at: Instant,
        @Serializable(with = InstantSerializer::class)
        val authenticated_at: Instant,
        val authenticator_assurance_level: String,
        val authentication_methods: ArrayList<AuthenticationMethod>,
        @Serializable(with = InstantSerializer::class)
        val issued_at: Instant,
        val identity: Identity,
        val devices: ArrayList<Device>
    )


    @Serializable
    class Traits (
        val name: String?,
        val email: String
    )

    @Serializable
    class Metadata (
        val roles: ArrayList<String>,
        val authorities: ArrayList<String>,
        val scope: ArrayList<String>,
        val sources: ArrayList<String>,
        val aud: ArrayList<String>,
        val mp_login: String?
    )
}
