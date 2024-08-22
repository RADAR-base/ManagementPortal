package org.radarbase.management.security

import io.ktor.http.*
import java.io.IOException
import java.time.Instant
import javax.annotation.Nonnull
import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpSession
import org.radarbase.auth.authentication.TokenValidator
import org.radarbase.auth.exception.TokenValidationException
import org.radarbase.auth.token.RadarToken
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.util.matcher.AntPathRequestMatcher
import org.springframework.web.cors.CorsUtils
import org.springframework.web.filter.OncePerRequestFilter

class JwtAuthenticationFilter(
        private val validator: TokenValidator,
        private val authenticationManager: AuthenticationManager,
        private val isOptional: Boolean = false
) : OncePerRequestFilter() {

    private val ignoreUrls: MutableList<AntPathRequestMatcher> = mutableListOf()

    fun skipUrlPattern(method: HttpMethod, vararg antPatterns: String?): JwtAuthenticationFilter {
        antPatterns.forEach { pattern ->
            pattern?.let { ignoreUrls.add(AntPathRequestMatcher(it, method.name)) }
        }
        return this
    }

    @Throws(IOException::class, ServletException::class)
    override fun doFilterInternal(
            httpRequest: HttpServletRequest,
            httpResponse: HttpServletResponse,
            chain: FilterChain,
    ) {
        logger.debug("Processing request: ${httpRequest.requestURI}")

        if (CorsUtils.isPreFlightRequest(httpRequest)) {
            logger.debug("Skipping JWT check for preflight request")
            chain.doFilter(httpRequest, httpResponse)
            return
        }

        val existingAuthentication = SecurityContextHolder.getContext().authentication
        val stringToken = tokenFromHeader(httpRequest)
        var token: RadarToken? = null

        if (stringToken != null) {
            token = validateTokenFromHeader(stringToken, httpRequest)
        }

        if (token == null && existingAuthentication.isAnonymous) {
            token = validateTokenFromSession(httpRequest.session)
        }

        if (!validateAndSetAuthentication(token, httpRequest, httpResponse)) {
            return
        }

        chain.doFilter(httpRequest, httpResponse)
    }

    private fun validateTokenFromHeader(
            tokenString: String,
            httpRequest: HttpServletRequest
    ): RadarToken? {
        return try {
            logger.debug("Validating token from header: ${tokenString}")
            val token = validator.validateBlocking(tokenString)
            val authentication = createAuthenticationFromToken(token)
            SecurityContextHolder.getContext().authentication = authentication
            logger.debug("JWT authentication successful")
            token
        } catch (ex: TokenValidationException) {
            logger.warn("Token validation failed: ${ex.message}")
            null
        }
    }

    private fun validateTokenFromSession(session: HttpSession?): RadarToken? {
        val token = session?.radarToken?.takeIf { Instant.now() < it.expiresAt }
        if (token != null) {
            logger.debug("Using token from session")
            val authentication = createAuthenticationFromToken(token)
            SecurityContextHolder.getContext().authentication = authentication
        }
        return token
    }

    private fun validateAndSetAuthentication(
            token: RadarToken?,
            httpRequest: HttpServletRequest,
            httpResponse: HttpServletResponse
    ): Boolean {
        return if (token != null) {
            httpRequest.radarToken = token
            val authentication = createAuthenticationFromToken(token)
            SecurityContextHolder.getContext().authentication = authentication
            true
        } else {
            handleUnauthorized(httpRequest, httpResponse, "No valid token provided")
            false
        }
    }

    private fun handleUnauthorized(
            httpRequest: HttpServletRequest,
            httpResponse: HttpServletResponse,
            message: String
    ) {
        if (!isOptional) {
            logger.error("Unauthorized - ${message}")
            httpResponse.returnUnauthorized(httpRequest, message)
        }
    }

    private fun createAuthenticationFromToken(token: RadarToken): Authentication {
        val authentication = RadarAuthentication(token)
        return authenticationManager.authenticate(authentication)
    }

    override fun shouldNotFilter(@Nonnull httpRequest: HttpServletRequest): Boolean {
        return ignoreUrls.any { it.matches(httpRequest) }.also { shouldSkip ->
            if (shouldSkip) {
                logger.debug("Skipping JWT check for ${httpRequest.requestURL}")
            }
        }
    }

    private fun tokenFromHeader(httpRequest: HttpServletRequest): String? {
        return httpRequest
                .getHeader(HttpHeaders.AUTHORIZATION)
                ?.takeIf { it.startsWith(AUTHORIZATION_BEARER_HEADER) }
                ?.removePrefix(AUTHORIZATION_BEARER_HEADER)
                ?.trim()
    }

    companion object {
        private val logger = LoggerFactory.getLogger(JwtAuthenticationFilter::class.java)
        private const val AUTHORIZATION_BEARER_HEADER = "Bearer"
        private const val TOKEN_ATTRIBUTE = "jwt"

        private fun HttpServletResponse.returnUnauthorized(
                request: HttpServletRequest,
                message: String?
        ) {
            status = HttpServletResponse.SC_UNAUTHORIZED
            setHeader(HttpHeaders.WWW_AUTHENTICATE, AUTHORIZATION_BEARER_HEADER)
            outputStream.print(
                    """
                {"error": "Unauthorized",
                "status": "${HttpServletResponse.SC_UNAUTHORIZED}",
                "message": "${message ?: "null"}",
                "path": "${request.requestURI}"}
                """.trimIndent()
            )
        }

        var HttpSession.radarToken: RadarToken?
            get() = getAttribute(TOKEN_ATTRIBUTE) as RadarToken?
            set(value) = setAttribute(TOKEN_ATTRIBUTE, value)

        var HttpServletRequest.radarToken: RadarToken?
            get() = getAttribute(TOKEN_ATTRIBUTE) as RadarToken?
            set(value) = setAttribute(TOKEN_ATTRIBUTE, value)

        val Authentication?.isAnonymous: Boolean
            get() {
                this ?: return true
                return authorities.size == 1 &&
                        authorities.firstOrNull()?.authority == "ROLE_ANONYMOUS"
            }
    }
}
