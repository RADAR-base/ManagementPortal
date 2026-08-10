package org.radarbase.management.config.annotations

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Configuration
@ConditionalOnProperty(
    name = ["managementportal.identityServer.internal"],
    havingValue = "false",
    matchIfMissing = false
)
annotation class IdentityServerDisabled

