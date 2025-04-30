package org.radarbase.management.security

import org.radarbase.auth.token.RadarToken
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.security.core.Authentication
import org.springframework.security.web.util.matcher.AntPathRequestMatcher
import org.springframework.web.filter.OncePerRequestFilter
import javax.annotation.Nonnull
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpSession

abstract class JwtAuthenticationFilter : OncePerRequestFilter() {

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

    protected fun tokenFromHeader(httpRequest: HttpServletRequest): String? {
        return httpRequest
            .getHeader(HttpHeaders.AUTHORIZATION)
            ?.takeIf { it.startsWith(AUTHORIZATION_BEARER_HEADER) }
            ?.removePrefix(AUTHORIZATION_BEARER_HEADER)
            ?.trim { it <= ' ' }
    }

    override fun shouldNotFilter(@Nonnull httpRequest: HttpServletRequest): Boolean {
        val shouldNotFilterUrl = ignoreUrls.find { it.matches(httpRequest) }
        return if (shouldNotFilterUrl != null) {
            log.debug("Skipping JWT check for ${httpRequest.requestURI}")
            true
        } else {
            false
        }
    }

    companion object {
        @JvmStatic
        protected fun HttpServletResponse.returnUnauthorized(request: HttpServletRequest) {
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

        @JvmStatic
        protected val log = LoggerFactory.getLogger(JwtAuthenticationFilterImpl::class.java)
        const val AUTHORIZATION_BEARER_HEADER = "Bearer"
        const val TOKEN_ATTRIBUTE = "jwt"

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