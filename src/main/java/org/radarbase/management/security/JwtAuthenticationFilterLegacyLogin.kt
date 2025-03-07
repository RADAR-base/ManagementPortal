package org.radarbase.management.security

import org.radarbase.auth.authentication.TokenValidator
import org.radarbase.auth.authorization.AuthorityReference
import org.radarbase.auth.authorization.RoleAuthority
import org.radarbase.auth.exception.TokenValidationException
import org.radarbase.auth.token.RadarToken
import org.radarbase.management.domain.Role
import org.radarbase.management.domain.User
import org.radarbase.management.repository.UserRepository
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.provider.OAuth2Authentication
import org.springframework.web.cors.CorsUtils
import java.io.IOException
import java.time.Instant
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
open class JwtAuthenticationFilterLegacyLogin @JvmOverloads constructor(
    private val validator: TokenValidator,
    private val authenticationManager: AuthenticationManager,
    private val userRepository: UserRepository,
    private val isOptional: Boolean = false
) : JwtAuthenticationFilter() {

    @Throws(IOException::class, ServletException::class)
    override fun doFilterInternal(
        httpRequest: HttpServletRequest,
        httpResponse: HttpServletResponse,
        chain: FilterChain,
    ) {
        if (CorsUtils.isPreFlightRequest(httpRequest)) {
            log.debug("Skipping JWT check for preflight request")
            chain.doFilter(httpRequest, httpResponse)
            return
        }

        val existingAuthentication = SecurityContextHolder.getContext().authentication

        if (existingAuthentication.isAnonymous || existingAuthentication is OAuth2Authentication) {
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
                    token = validator.validateBlocking(stringToken)
                    log.debug("Using token from header")
                } catch (ex: TokenValidationException) {
                    ex.message?.let { exMessage = it }
                    log.info("Failed to validate token from header: {}", exMessage)
                }
            }
            if (!validateToken(token, httpRequest, httpResponse, session, exMessage)) {
                return
            }
        }
        chain.doFilter(httpRequest, httpResponse)
    }

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
            log.debug("Skipping optional token")
            true
        } else {
            log.error("Unauthorized - no valid token provided")
            httpResponse.returnUnauthorized(httpRequest)
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
            httpResponse.returnUnauthorized(httpRequest)
            null
        }
    }

    companion object {

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

    }
}
