package org.radarbase.management.web.rest

import kotlinx.coroutines.runBlocking
import org.radarbase.management.service.SessionToJwtService
import org.radarbase.management.web.rest.errors.BadRequestException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest

/**
 * REST controller for converting Kratos session tokens to JWT tokens.
 * Uses ORY Kratos session-to-JWT functionality.
 * Only available when external auth server is configured (managementportal.authServer.internal = false).
 */
@RestController
@RequestMapping("/api")
@ConditionalOnProperty(
    name = ["managementportal.authServer.internal"],
    havingValue = "false"
)
class TokenExchangeResource @Autowired constructor(
    private val sessionToJwtService: SessionToJwtService,
) {

    /**
     * POST /api/token-exchange : Convert Kratos session token to JWT token
     *
     * @param request the HTTP request containing the session token
     * @param parameters the form parameters containing the session token
     * @return the ResponseEntity with status 200 (OK) and the JWT token in body,
     * or with status 400 (Bad Request) if the session token is invalid,
     * or with status 500 (Internal Server Error) if the conversion fails
     */
    @PostMapping(value = ["/token-exchange"],
        consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE]
    )
    @Throws(
        HttpRequestMethodNotSupportedException::class
    )
    fun exchangeToken(
        request: HttpServletRequest,
        @RequestParam parameters: Map<String, String>
    ): ResponseEntity<TokenExchangeResponse> {
        val sessionToken: String? = parameters.get("session_token")
        if (sessionToken == null) {
            throw BadRequestException("Session token is required", ENTITY_NAME, "sessionTokenRequired")
        }
        logger.debug("Session to JWT conversion request received for session: {}", sessionToken.take(10) + "...")

        return try {
            runBlocking {
                val jwtToken = sessionToJwtService.convertSessionToJwt(sessionToken)
                logger.debug("Successfully converted session to JWT")

                ResponseEntity.ok(TokenExchangeResponse(
                    accessToken = jwtToken.accessToken,
                    tokenType = jwtToken.tokenType,
                    expiresIn = jwtToken.expiresIn,
                    scope = jwtToken.scope
                ))
            }
        } catch (e: Exception) {
            logger.error("Failed to convert session to JWT", e)
            throw BadRequestException("Session to JWT conversion failed: ${e.message}", ENTITY_NAME, "sessionToJwtFailed")
        }
    }

    data class TokenExchangeResponse(
        val accessToken: String,
        val tokenType: String = "Bearer",
        val expiresIn: Long?,
        val scope: String?
    )

    companion object {
        private val logger = LoggerFactory.getLogger(TokenExchangeResource::class.java)
        private const val ENTITY_NAME = "tokenExchange"
    }
}
