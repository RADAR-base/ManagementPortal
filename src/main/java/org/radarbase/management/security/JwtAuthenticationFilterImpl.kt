package org.radarbase.management.security

import org.radarbase.auth.authentication.TokenValidator
import org.radarbase.auth.exception.TokenValidationException
import org.radarbase.auth.token.RadarToken
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.cors.CorsUtils
import java.io.IOException
import java.time.Instant
import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class JwtAuthenticationFilterImpl(
        private val validator: TokenValidator,
        private val authenticationManager: AuthenticationManager,
        private val isOptional: Boolean = false
) : JwtAuthenticationFilter() {

    @Throws(IOException::class, ServletException::class)
    override fun doFilterInternal(
            httpRequest: HttpServletRequest,
            httpResponse: HttpServletResponse,
            chain: FilterChain
    ) {
        try {
            if (CorsUtils.isPreFlightRequest(httpRequest)) {
                log.debug("Skipping JWT check for ${httpRequest.requestURI}")
                chain.doFilter(httpRequest, httpResponse)
                return
            }
            val session = httpRequest.getSession(false)
            val stringToken = tokenFromHeader(httpRequest)
            var token: RadarToken? = null
            var exMessage = "No token provided"
            token = session?.radarToken
                ?.takeIf { Instant.now() < it.expiresAt }
            if (token != null) {
                log.debug("Using token from session")
            }
            else if (stringToken != null) {
                try {
                    log.warn("Validating token from header: $stringToken")
                    token = validator.validateBlocking(stringToken)
                    val authentication = createAuthenticationFromToken(token)
                    SecurityContextHolder.getContext().authentication = authentication
                    log.debug("JWT authentication successful")
                } catch (ex: TokenValidationException) {
                    exMessage = ex.message ?: exMessage
                    log.info("Failed to validate token from header: $exMessage")
                }
            }

            if (token == null) {
                val existingAuthentication = SecurityContextHolder.getContext().authentication
                if (existingAuthentication != null &&
                                existingAuthentication.isAuthenticated &&
                                !existingAuthentication.isAnonymous
                ) {
                    chain.doFilter(httpRequest, httpResponse)
                    return
                }

                val session = httpRequest.getSession(false)
                token = session?.radarToken?.takeIf { Instant.now() < it.expiresAt }
                if (token != null) {
                    log.debug("Using token from session")
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
            log.debug("Skipping optional token check for ${httpRequest.requestURI}")
            true
        } else {
            log.error("Unauthorized - no valid token provided for ${httpRequest.requestURI}")
            httpResponse.returnUnauthorized(httpRequest)
            false
        }
    }

}
