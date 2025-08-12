package org.radarbase.management.service

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.radarbase.auth.kratos.KratosJWTDTO
import org.radarbase.management.config.ManagementPortalProperties
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import java.time.Duration

/**
 * Service for converting Kratos session tokens to JWT tokens.
 * Uses ORY Kratos session-to-JWT functionality.
 * Only available when external auth server is configured.
 */
@Service
@ConditionalOnProperty(
    name = ["managementportal.authServer.internal"],
    havingValue = "false"
)
class SessionToJwtService(
    private val managementPortalProperties: ManagementPortalProperties
) {

    /**
     * Convert a Kratos session token to a JWT token using Kratos session-to-JWT endpoint.
     *
     * @param sessionToken the Kratos session token
     * @return the JWT token response
     */
    suspend fun convertSessionToJwt(sessionToken: String): JwtTokenResponse {
        logger.debug("Converting Kratos session token to JWT")

        val jwtEndpoint = "${managementPortalProperties.identityServer.serverUrl}/sessions/whoami"

        return withContext(Dispatchers.IO) {
            val response = httpClient.get {
                url(jwtEndpoint)
                parameter("tokenize_as", "mp_jwt_template")
                header("Authorization", "Bearer $sessionToken")
                accept(ContentType.Application.Json)
            }

            if (response.status.isSuccess()) {
                val jwtResponse = response.body<KratosJWTDTO>()
                logger.debug("Successfully converted session to JWT")

                JwtTokenResponse(
                    accessToken = jwtResponse.tokenized,
                    tokenType = "Bearer",
                    expiresIn = jwtResponse.expires_at.epochSecond,
                    scope = extractScopesFromJwtResponse(jwtResponse)
                )
            } else {
                val errorBody = try {
                    response.body<String>()
                } catch (e: Exception) {
                    "Unable to read response body: ${e.message}"
                }
                logger.error("Session to JWT conversion failed with status: {}, body: {}", response.status, errorBody)
                throw SessionToJwtException("Session to JWT conversion failed: ${response.status} - $errorBody")
            }
        }
    }

     /**
     * Extract scopes from Kratos JWT response metadata.
     */
    private fun extractScopesFromJwtResponse(jwtResponse: KratosJWTDTO?): String? {
        return jwtResponse?.identity?.metadata_public?.scope?.joinToString(" ")?.takeIf { it.isNotEmpty() }
    }

    @Serializable
    data class JwtTokenResponse(
        val accessToken: String,
        val tokenType: String = "Bearer",
        val expiresIn: Long? = null,
        val scope: String? = null
    )

    class SessionToJwtException(message: String, cause: Throwable? = null) : Exception(message, cause)

    companion object {
        private val logger = LoggerFactory.getLogger(SessionToJwtService::class.java)

        private val httpClient = HttpClient(CIO).config {
            install(HttpTimeout) {
                connectTimeoutMillis = Duration.ofSeconds(10).toMillis()
                socketTimeoutMillis = Duration.ofSeconds(10).toMillis()
                requestTimeoutMillis = Duration.ofSeconds(30).toMillis()
            }
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    coerceInputValues = true
                })
            }
        }
    }
}
