package org.radarbase.management.security

import org.radarbase.auth.authentication.TokenValidator
import org.radarbase.auth.authorization.AuthorityReference
import org.radarbase.auth.authorization.RoleAuthority
import org.radarbase.auth.exception.TokenValidationException
import org.radarbase.auth.token.RadarToken
import org.radarbase.management.domain.Role
import org.radarbase.management.domain.User
import org.radarbase.management.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.provider.OAuth2Authentication
import org.springframework.security.web.util.matcher.AntPathRequestMatcher
import org.springframework.web.cors.CorsUtils
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException
import java.time.Instant
import jakarta.annotation.Nonnull
import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpSession

/**
 * Authentication filter using given validator.
 * @param validator validates the JWT token.
 * @param authenticationManager authentication manager to pass valid authentication to.
 * @param userRepository user repository to retrieve user details from.
 * @param isOptional do not fail if no authentication is provided
 */
class JwtAuthenticationFilter @JvmOverloads constructor(
    private val validator: TokenValidator,
    private val authenticationManager: AuthenticationManager,
    private val userRepository: UserRepository,
    private val isOptional: Boolean = false
) : OncePerRequestFilter() {
    private val ignoreUrls: MutableList<AntPathRequestMatcher> = mutableListOf()

    /**
     * Do not use JWT authentication for given paths and HTTP method.
     * @param method HTTP method
     * @param antPatterns Ant wildcard pattern
     * @return the current filter
     */
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
        chain: FilterChain,
    ) {
        if (CorsUtils.isPreFlightRequest(httpRequest)) {
            Companion.logger.debug("Skipping JWT check for preflight request")
            chain.doFilter(httpRequest, httpResponse)
            return
        }

        val existingAuthentication = SecurityContextHolder.getContext().authentication

        if (existingAuthentication.isAnonymous || existingAuthentication is OAuth2Authentication) {
            val session = httpRequest.getSession(false)
            val stringToken = tokenFromHeader(httpRequest)
            var token: RadarToken? = null
            var exMessage = "No token provided"
            if (stringToken != null) {
                try {
                    token = validator.validateBlocking(stringToken)
                    Companion.logger.debug("Using token from header")
                } catch (ex: TokenValidationException) {
                    ex.message?.let { exMessage = it }
                    Companion.logger.info("Failed to validate token from header: {}", exMessage)
                }
            }
            if (token == null) {
                token = session?.radarToken
                    ?.takeIf { Instant.now() < it.expiresAt }
                if (token != null) {
                    Companion.logger.debug("Using token from session")
                }
            }
            if (!validateToken(token, httpRequest, httpResponse, session, exMessage)) {
                return
            }
        }
        chain.doFilter(httpRequest, httpResponse)
    }

    override fun shouldNotFilter(@Nonnull httpRequest: HttpServletRequest): Boolean {
        val shouldNotFilterUrl = ignoreUrls.find { it.matches(httpRequest) }
        return if (shouldNotFilterUrl != null) {
            Companion.logger.debug("Skipping JWT check for {} request", shouldNotFilterUrl)
            true
        } else {
            false
        }
    }

    private fun tokenFromHeader(httpRequest: HttpServletRequest): String? =
        httpRequest.getHeader(HttpHeaders.AUTHORIZATION)
            ?.takeIf { it.startsWith(AUTHORIZATION_BEARER_HEADER) }
            ?.removePrefix(AUTHORIZATION_BEARER_HEADER)
            ?.trim { it <= ' ' }

    @Throws(IOException::class)
    private fun validateToken(
        token: RadarToken?,
        httpRequest: HttpServletRequest,
        httpResponse: HttpServletResponse,
        session: HttpSession?,
        exMessage: String?,
    ): Boolean {
        return if (token != null) {
            val updatedToken = checkUser(token, httpRequest, httpResponse, session)
                ?: return false
            httpRequest.radarToken = updatedToken
            val authentication = RadarAuthentication(updatedToken)
            authenticationManager.authenticate(authentication)
            SecurityContextHolder.getContext().authentication = authentication
            true
        } else if (isOptional) {
            logger.debug("Skipping optional token")
            true
        } else {
            logger.error("Unauthorized - no valid token provided")
            httpResponse.returnUnauthorized(httpRequest, exMessage)
            false
        }
    }

    @Throws(IOException::class)
    private fun checkUser(
        token: RadarToken,
        httpRequest: HttpServletRequest,
        httpResponse: HttpServletResponse,
        session: HttpSession?,
    ): RadarToken? {
        val userName = token.username ?: return token
        val user = userRepository.findOneByLogin(userName)
        return if (user != null) {
            token.copyWithRoles(user.authorityReferences)
        } else {
            session?.removeAttribute(TOKEN_ATTRIBUTE)
            httpResponse.returnUnauthorized(httpRequest, "User not found")
            null
        }
    }

    companion object {
        private fun HttpServletResponse.returnUnauthorized(request: HttpServletRequest, message: String?) {
            status = HttpServletResponse.SC_UNAUTHORIZED
            setHeader(HttpHeaders.WWW_AUTHENTICATE, AUTHORIZATION_BEARER_HEADER)
            val fullMessage = if (message != null) {
                "\"$message\""
            } else {
                "null"
            }
            outputStream.print(
                """
                {"error": "Unauthorized",
                "status": "${HttpServletResponse.SC_UNAUTHORIZED}",
                message": $fullMessage,
                "path": "${request.requestURI}"}
                """.trimIndent()
            )
        }

        private val logger = LoggerFactory.getLogger(JwtAuthenticationFilter::class.java)
        private const val AUTHORIZATION_BEARER_HEADER = "Bearer"
        private const val TOKEN_ATTRIBUTE = "jwt"

        /**
         * Authority references for given user. The user should have its roles mapped
         * from the database.
         * @return set of authority references.
         */
        val User.authorityReferences: Set<AuthorityReference>
            get() = roles.mapTo(HashSet()) { role: Role? ->
                val auth = role?.role
                val referent = when (auth?.scope) {
                    RoleAuthority.Scope.GLOBAL -> null
                    RoleAuthority.Scope.ORGANIZATION -> role.organization?.name
                    RoleAuthority.Scope.PROJECT -> role.project?.projectName
                    null -> null
                }
                AuthorityReference(auth!!, referent)
            }



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
