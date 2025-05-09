package org.radarbase.management.config

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Configuration
@ConditionalOnProperty(
    name = ["managementportal.authServer.internal"],
    havingValue = "true",
    matchIfMissing = true
)
annotation class AuthServerEnabled