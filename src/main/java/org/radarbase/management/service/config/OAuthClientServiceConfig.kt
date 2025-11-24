package org.radarbase.management.service.config

import org.radarbase.management.config.ManagementPortalProperties
import org.radarbase.management.service.DefaultOAuthClientService
import org.radarbase.management.service.OAuthClientService
import org.radarbase.management.service.HydraOAuthClientService
import org.radarbase.management.service.mapper.ClientDetailsMapper
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerEndpointsConfiguration
import org.springframework.security.oauth2.provider.client.JdbcClientDetailsService

@Configuration
class OAuthClientServiceConfiguration {

    @Bean
    @Primary
    fun oAuthClientService(
        managementPortalProperties: ManagementPortalProperties,
        jdbcClientDetailsService: JdbcClientDetailsService?,
        clientDetailsMapper: ClientDetailsMapper?,
        authorizationServerEndpointsConfiguration: AuthorizationServerEndpointsConfiguration?
    ): OAuthClientService {
        return if (managementPortalProperties.authServer.internal) {
            log.info("Using internal OAuth client management")
            require(jdbcClientDetailsService != null) {
                "JdbcClientDetailsService must be available when using internal OAuth server"
            }
            require(clientDetailsMapper != null) {
                "ClientDetailsMapper must be available when using internal OAuth server"
            }
            require(authorizationServerEndpointsConfiguration != null) {
                "AuthorizationServerEndpointsConfiguration must be available when using internal OAuth server"
            }
            DefaultOAuthClientService(
                jdbcClientDetailsService,
                clientDetailsMapper,
                authorizationServerEndpointsConfiguration
            )
        } else {
            log.info("Using Hydra external OAuth client management")
            // Validate Hydra configuration
            require(managementPortalProperties.authServer.serverUrl.isNotBlank()) {
                "Hydra server URL must be configured when using external OAuth management"
            }
            require(managementPortalProperties.authServer.serverAdminUrl.isNotBlank()) {
                "Hydra admin server URL must be configured when using external OAuth management"
            }
            HydraOAuthClientService(managementPortalProperties)
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(OAuthClientServiceConfiguration::class.java)
    }
}
