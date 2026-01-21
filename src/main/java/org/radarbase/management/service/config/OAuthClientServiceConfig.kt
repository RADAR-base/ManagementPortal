package org.radarbase.management.service.config

import org.radarbase.management.config.annotations.AuthServerEnabled
import org.radarbase.management.config.annotations.AuthServerDisabled
import org.radarbase.management.config.ManagementPortalProperties
import org.radarbase.management.service.DefaultOAuthClientService
import org.radarbase.management.service.OAuthClientService
import org.radarbase.management.service.HydraOAuthClientService
import org.radarbase.management.service.mapper.ClientDetailsMapper
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerEndpointsConfiguration
import org.springframework.security.oauth2.provider.client.JdbcClientDetailsService

@Configuration
class OAuthClientServiceConfiguration {

    @AuthServerEnabled
    @Configuration
    class DefaultOAuthClientServiceConfiguration {
        @Bean
        fun defaultOAuthClientService(
            jdbcClientDetailsService: JdbcClientDetailsService,
            clientDetailsMapper: ClientDetailsMapper,
            authorizationServerEndpointsConfiguration: AuthorizationServerEndpointsConfiguration
        ): OAuthClientService {
            log.info("Using internal OAuth client management")
            return DefaultOAuthClientService(
                jdbcClientDetailsService,
                clientDetailsMapper,
                authorizationServerEndpointsConfiguration
            )
        }
    }

    @AuthServerDisabled
    @Configuration
    class HydraOAuthClientServiceConfiguration {
        @Bean
        fun hydraOAuthClientService(
            managementPortalProperties: ManagementPortalProperties
        ): OAuthClientService {
            log.info("Using Hydra external OAuth client management")
            // Validate Hydra configuration
            require(managementPortalProperties.authServer.serverUrl.isNotBlank()) {
                "Hydra server URL must be configured when using external OAuth management"
            }
            require(managementPortalProperties.authServer.serverAdminUrl.isNotBlank()) {
                "Hydra admin server URL must be configured when using external OAuth management"
            }
            return HydraOAuthClientService(managementPortalProperties)
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(OAuthClientServiceConfiguration::class.java)
    }
}
