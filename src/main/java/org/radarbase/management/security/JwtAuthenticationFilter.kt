package org.radarbase.management.security

import java.io.IOException
import java.time.Instant
import javax.annotation.Nonnull
import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpSession
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

class JwtAuthenticationFilter(
    private val validator: TokenValidator,
    private val authenticationManager: AuthenticationManager,
    private val enableUserLookup: Boolean = false,
    private val userRepository: UserRepository? = null,
    private val isOptional: Boolean = false
) : OncePerRequestFilter() {

    private val ignoreUrls: MutableList<AntPathRequestMatcher> = mutableListOf()

    fun skipUrlPattern(method: HttpMethod, vararg antPatterns: String?): JwtAuthenticationFilter {
        for (pattern in antPatterns) {
            ignoreUrls.add(AntPathRequestMatcher(pattern, method.name))
        }
        return this
    }

    override fun shouldNotFilter(@Nonnull request: HttpServletRequest): Boolean {
        return ignoreUrls.any { it.matches(request) }
    }

    @Throws(IOException::class, ServletException::class)
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        chain: FilterChain,
    ) {
        if (CorsUtils.isPreFlightRequest(request)) {
            chain.doFilter(request, response)
            return
        }

        val existingAuth = SecurityContextHolder.getContext().authentication
        if (!existingAuth.isAnonymous && existingAuth !is OAuth2Authentication) {
            chain.doFilter(request, response)
            return
        }

        val session = request.getSession(false)
        val stringToken = tokenFromHeader(request)
        var token: RadarToken? = session?.radarToken?.takeIf { Instant.now() < it.expiresAt }

        var exMessage = "No token provided"
        if (token == null && stringToken != null) {
            try {
                token = validator.validateBlocking(stringToken)
            } catch (ex: TokenValidationException) {
                exMessage = ex.message ?: exMessage
                logger.info("Token validation failed: $exMessage")
            }
        }

        if (!validateToken(token, request, response, session, exMessage)) {
            return
        }

        chain.doFilter(request, response)
        SecurityContextHolder.clearContext()
    }

    private fun tokenFromHeader(request: HttpServletRequest): String? {
        return request.getHeader(HttpHeaders.AUTHORIZATION)
            ?.takeIf { it.startsWith(AUTHORIZATION_BEARER_HEADER) }
            ?.removePrefix(AUTHORIZATION_BEARER_HEADER)
            ?.trim()
    }

    private fun validateToken(
        token: RadarToken?,
        request: HttpServletRequest,
        response: HttpServletResponse,
        session: HttpSession?,
        exMessage: String?,
    ): Boolean {
        return if (token != null) {
            val effectiveToken = if (enableUserLookup) {
                checkUser(token, request, response, session) ?: return false
            } else {
                token
            }

            request.radarToken = effectiveToken
            val auth = RadarAuthentication(effectiveToken)
            authenticationManager.authenticate(auth)
            SecurityContextHolder.getContext().authentication = auth
            true
        } else if (isOptional) {
            logger.debug("No token, but skipping due to optional auth")
            true
        } else {
            logger.error("Unauthorized - $exMessage")
            response.returnUnauthorized(request, exMessage)
            false
        }
    }

    private fun checkUser(
        token: RadarToken,
        request: HttpServletRequest,
        response: HttpServletResponse,
        session: HttpSession?,
    ): RadarToken? {
        val username = token.username ?: return token
        val user = userRepository?.findOneByLogin(username)
        return if (user != null) {
            token.copyWithRoles(user.authorityReferences)
        } else {
            session?.removeAttribute(TOKEN_ATTRIBUTE)
            response.returnUnauthorized(request, "User not found")
            null
        }
    }

    companion object {
        private const val AUTHORIZATION_BEARER_HEADER = "Bearer"
        private const val TOKEN_ATTRIBUTE = "jwt"
        private val logger = LoggerFactory.getLogger(JwtAuthenticationFilter::class.java)

        private fun HttpServletResponse.returnUnauthorized(
            request: HttpServletRequest,
            message: String?,
        ) {
            status = HttpServletResponse.SC_UNAUTHORIZED
            setHeader(HttpHeaders.WWW_AUTHENTICATE, AUTHORIZATION_BEARER_HEADER)
            val msg = message?.replace("\"", "\\\"")
            outputStream.print(
                """
                {"error": "Unauthorized",
                 "status": 401,
                 "message": "$msg",
                 "path": "${request.requestURI}"}
                """.trimIndent()
            )
        }

        private val Authentication?.isAnonymous: Boolean
            get() {
                this ?: return true
                return authorities.size == 1 &&
                    authorities.firstOrNull()?.authority == "ROLE_ANONYMOUS"
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

        val User.authorityReferences: Set<AuthorityReference>
            get() = roles.mapTo(HashSet()) { role ->
                val auth = role?.role
                val referent = when (auth?.scope) {
                    RoleAuthority.Scope.GLOBAL -> null
                    RoleAuthority.Scope.ORGANIZATION -> role.organization?.name
                    RoleAuthority.Scope.PROJECT -> role.project?.projectName
                    else -> null
                }
                AuthorityReference(auth!!, referent)
            }
    }
}