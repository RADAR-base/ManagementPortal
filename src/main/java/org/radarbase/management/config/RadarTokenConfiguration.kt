package org.radarbase.management.config

import org.radarbase.auth.token.RadarToken
import org.radarbase.management.security.jwt.RadarTokenLoader
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Scope
import org.springframework.context.annotation.ScopedProxyMode
import javax.servlet.http.HttpServletRequest

@Configuration
open class RadarTokenConfiguration @Autowired constructor(private val radarTokenLoader: RadarTokenLoader) {
    @Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
    @Bean
    open fun radarToken(request: HttpServletRequest?): RadarToken? {
        return radarTokenLoader.loadToken(request!!)
    }
}
