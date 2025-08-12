package org.radarbase.management.service.config

import org.radarbase.auth.kratos.SessionService
import org.radarbase.management.config.ManagementPortalProperties
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Configuration for SessionService when external identity server (Kratos) is used.
 */
@Configuration
class SessionServiceConfig {

    /**
     * Provides SessionService bean when external auth server is configured.
     * Only available when managementportal.authServer.internal = false.
     */
    @Bean
    @ConditionalOnProperty(
        name = ["managementportal.authServer.internal"],
        havingValue = "false"
    )
    fun sessionService(managementPortalProperties: ManagementPortalProperties): SessionService {
        log.info("Configuring Kratos SessionService with server URL: {}",
                managementPortalProperties.identityServer.serverUrl)

        require(managementPortalProperties.identityServer.serverUrl.isNotBlank()) {
            "Kratos server URL must be configured when using external identity management"
        }

        return SessionService(managementPortalProperties.identityServer.serverUrl)
    }

    companion object {
        private val log = LoggerFactory.getLogger(SessionServiceConfig::class.java)
    }
}
