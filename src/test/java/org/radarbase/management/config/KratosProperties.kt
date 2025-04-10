package org.radarbase.management.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "kratos")
class KratosProperties {
    lateinit var publicUrl: String
    lateinit var adminUrl: String
}
