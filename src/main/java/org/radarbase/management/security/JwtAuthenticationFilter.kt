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
        for (pattern in antPatterns) {
            ignoreUrls.add(AntPathRequestMatcher(pattern, method.name))
        }
        return this
    }

    @Throws(IOException::class, ServletException::class)
    override fun doFilterInternal(
            httpRequest: HttpServletRequest,
            httpResponse: HttpServletResponse,
            chain: FilterChain
    ) {
        try {
            if (CorsUtils.isPreFlightRequest(httpRequest)) {
                logger.debug("Skipping JWT check for ${httpRequest.requestURI}")
                chain.doFilter(httpRequest, httpResponse)
                return
            }
            val stringToken = tokenFromHeader(httpRequest)
            var token: RadarToken? = null
            var exMessage = "No token provided"

            if (stringToken != null) {
                try {
                    logger.warn("Validating token from header: $stringToken")
                    token = validator.validateBlocking(stringToken)
                    val authentication = createAuthenticationFromToken(token)
                    SecurityContextHolder.getContext().authentication = authentication
                    logger.debug("JWT authentication successful")
                } catch (ex: TokenValidationException) {
                    exMessage = ex.message ?: exMessage
                    logger.info("Failed to validate token from header: $exMessage")
                }
            }

            if (token == null) {
                val existingAuthentication = SecurityContextHolder.getContext().authentication
                if (existingAuthentication != null &&
                                existingAuthentication.isAuthenticated &&
                                !existingAuthentication.isAnonymous
                ) {
                    logger.info("Existing authentication found: ${existingAuthentication}")
                    chain.doFilter(httpRequest, httpResponse)
                    return
                }

                val session = httpRequest.getSession(false)
                token = session?.radarToken?.takeIf { Instant.now() < it.expiresAt }
                if (token != null) {
                    logger.debug("Using token from session")
                    val authentication = createAuthenticationFromToken(token)
                    SecurityContextHolder.getContext().authentication = authentication
                }
            }

            if (!validateToken(token, httpRequest, httpResponse)) {
                return
            }
            chain.doFilter(httpRequest, httpResponse)
        } finally {
            SecurityContextHolder.clearContext()
        }
    }

    private fun createAuthenticationFromToken(token: RadarToken): Authentication {
        val authentication = RadarAuthentication(token)
        return authenticationManager.authenticate(authentication)
    }

    override fun shouldNotFilter(@Nonnull httpRequest: HttpServletRequest): Boolean {
        val shouldNotFilterUrl = ignoreUrls.find { it.matches(httpRequest) }
        return if (shouldNotFilterUrl != null) {
            logger.debug("Skipping JWT check for ${httpRequest.requestURI}")
            true
        } else {
            false
        }
    }

    private fun tokenFromHeader(httpRequest: HttpServletRequest): String? {
        return httpRequest
                .getHeader(HttpHeaders.AUTHORIZATION)
                ?.takeIf { it.startsWith(AUTHORIZATION_BEARER_HEADER) }
                ?.removePrefix(AUTHORIZATION_BEARER_HEADER)
                ?.trim { it <= ' ' }
    }

    private fun validateToken(
            token: RadarToken?,
            httpRequest: HttpServletRequest,
            httpResponse: HttpServletResponse,
    ): Boolean {
        return if (token != null) {
            httpRequest.radarToken = token
            val authentication = RadarAuthentication(token)
            authenticationManager.authenticate(authentication)
            SecurityContextHolder.getContext().authentication = authentication
            true
        } else if (isOptional) {
            logger.debug("Skipping optional token check for ${httpRequest.requestURI}")
            true
        } else {
            logger.error("Unauthorized - no valid token provided for ${httpRequest.requestURI}")
            httpResponse.returnUnauthorized(httpRequest)
            false
        }
    }

    companion object {
        private fun HttpServletResponse.returnUnauthorized(request: HttpServletRequest) {
            status = HttpServletResponse.SC_UNAUTHORIZED
            setHeader(HttpHeaders.WWW_AUTHENTICATE, AUTHORIZATION_BEARER_HEADER)
            outputStream.print(
                    """
                {"error": "Unauthorized",
                "status": "${HttpServletResponse.SC_UNAUTHORIZED}",
                "path": "${request.requestURI}"}
            """.trimIndent()
            )
        }

        private val logger = LoggerFactory.getLogger(JwtAuthenticationFilter::class.java)
        private const val AUTHORIZATION_BEARER_HEADER = "Bearer"
        private const val TOKEN_ATTRIBUTE = "jwt"

        @get:JvmStatic
        @set:JvmStatic
        var HttpSession.radarToken: RadarToken?
            get() = getAttribute(TOKEN_ATTRIBUTE) as RadarToken?
            set(value) = setAttribute(TOKEN_ATTRIBUTE, value)

        @get:JvmStatic
        @set:JvmStatic
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
