package org.radarbase.management.security.jwt

import org.radarbase.auth.token.RadarToken
import org.radarbase.management.security.JwtAuthenticationFilter.Companion.radarToken
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import javax.servlet.http.HttpServletRequest

@Component
class RadarTokenLoader {
    fun loadToken(httpServletRequest: HttpServletRequest): RadarToken? =
        httpServletRequest.radarToken
            ?.also { logger.debug("Using request RadarToken") }
            ?: httpServletRequest.getSession(false)?.radarToken
                ?.also { logger.debug("Using session RadarToken") }

    companion object {
        private val logger = LoggerFactory.getLogger(RadarTokenLoader::class.java)
    }
}
