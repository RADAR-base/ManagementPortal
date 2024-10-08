package org.radarbase.management.service

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import java.time.Duration
import java.util.*
import java.util.function.Consumer
import javax.annotation.Nullable
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonPrimitive
import org.radarbase.auth.authorization.*
import org.radarbase.auth.exception.IdpException
import org.radarbase.auth.token.RadarToken
import org.radarbase.management.config.ManagementPortalProperties
import org.radarbase.management.security.NotAuthorizedException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class AuthService(
        @Nullable private val token: RadarToken?,
        private val oracle: AuthorizationOracle,
        @Autowired private val managementPortalProperties: ManagementPortalProperties,
) {
    private val httpClient =
            HttpClient(CIO) {
                install(HttpTimeout) {
                    connectTimeoutMillis = Duration.ofSeconds(20).toMillis()
                    socketTimeoutMillis = Duration.ofSeconds(20).toMillis()
                    requestTimeoutMillis = Duration.ofSeconds(300).toMillis()
                }
                install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
            }
    /**
     * Check whether given [token] would have the [permission] scope in any of its roles. This
     * doesn't check whether [token] has access to a specific entity or global access.
     * @throws NotAuthorizedException if identity does not have scope
     */
    @Throws(NotAuthorizedException::class)
    fun checkScope(permission: Permission) {
        val token =
                token
                        ?: throw NotAuthorizedException(
                                "User without authentication does not have permission."
                        )

        if (!oracle.hasScope(token, permission)) {
            throw NotAuthorizedException(
                    "User ${token.username} with client ${token.clientId} does not have permission $permission"
            )
        }
    }

    /**
     * Check whether [token] has permission [permission], regarding given entity from [builder]. The
     * permission is checked both for its own entity scope and for the
     * [EntityDetails.minimumEntityOrNull] entity scope.
     * @throws NotAuthorizedException if identity does not have permission
     */
    @JvmOverloads
    @Throws(NotAuthorizedException::class)
    fun checkPermission(
            permission: Permission,
            builder: Consumer<EntityDetails>? = null,
            scope: Permission.Entity = permission.entity,
    ) {
        val token =
                token
                        ?: throw NotAuthorizedException(
                                "User without authentication does not have permission."
                        )

        // entitydetails builder is null means we require global permission
        val entity = if (builder != null) entityDetailsBuilder(builder) else EntityDetails.global

        val hasPermission = runBlocking { oracle.hasPermission(token, permission, entity, scope) }
        if (!hasPermission) {
            throw NotAuthorizedException(
                    "User ${token.username} with client ${token.clientId} does not have permission $permission to scope " +
                            "$scope of $entity"
            )
        }
    }

    fun referentsByScope(permission: Permission): AuthorityReferenceSet {
        val token = token ?: return AuthorityReferenceSet()
        return oracle.referentsByScope(token, permission)
    }

    fun mayBeGranted(role: RoleAuthority, permission: Permission): Boolean =
            with(oracle) { role.mayBeGranted(permission) }

    fun mayBeGranted(authorities: Collection<RoleAuthority>, permission: Permission): Boolean {
        return authorities.any { mayBeGranted(it, permission) }
    }

    suspend fun fetchAccessToken(code: String): String {
        val tokenUrl = "${managementPortalProperties.authServer.serverUrl}/oauth2/token"
        val clientId = managementPortalProperties.frontend.clientId
        val clientSecret = managementPortalProperties.frontend.clientSecret
        val authHeader = "Basic " + Base64.getEncoder().encodeToString("$clientId:$clientSecret".toByteArray())
        val response =
                httpClient.post(tokenUrl) {
                    headers { append(HttpHeaders.Authorization, authHeader) }
                    contentType(ContentType.Application.FormUrlEncoded)
                    accept(ContentType.Application.Json)
                    setBody(
                            Parameters.build {
                                        append("grant_type", "authorization_code")
                                        append("code", code)
                                        append(
                                                "redirect_uri",
                                                "${managementPortalProperties.common.managementPortalBaseUrl}/api/redirect/login"
                                        )
                                        append(
                                                "client_id",
                                                clientId
                                        )
                                    }
                                    .formUrlEncode(),
                    )
                }

        if (response.status.isSuccess()) {
            val responseMap = response.body<Map<String, JsonElement>>()
            return responseMap["access_token"]?.jsonPrimitive?.content
                    ?: throw IdpException("Access token not found in response")
        } else {
            throw IdpException("Unable to get access token")
        }
    }
}
