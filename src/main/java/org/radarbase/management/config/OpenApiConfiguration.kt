/*
 * Copyright (c) 2021. The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 */
package org.radarbase.management.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.security.OAuthFlow
import io.swagger.v3.oas.models.security.OAuthFlows
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import tech.jhipster.config.JHipsterConstants

@Configuration
@Profile(JHipsterConstants.SPRING_PROFILE_API_DOCS)
class OpenApiConfiguration {
    @Bean
    fun customOpenAPI(): OpenAPI {
        return OpenAPI()
            .components(
                Components()
                    .addSecuritySchemes(
                        "oauth2Login", SecurityScheme()
                            .type(SecurityScheme.Type.OAUTH2)
                            .flows(
                                OAuthFlows()
                                    .authorizationCode(
                                        OAuthFlow()
                                            .authorizationUrl("/oauth/authorize")
                                            .tokenUrl("/oauth/token")
                                    )
                                    .clientCredentials(
                                        OAuthFlow()
                                            .tokenUrl("/oauth/token")
                                    )
                            )
                    )
            )
            .info(
                Info()
                    .title("ManagementPortal API")
                    .description("ManagementPortal for RADAR-base")
                    .license(
                        License()
                            .name("Apache 2.0")
                            .url("https://radar-base.org")
                    )
            )
    }
}
