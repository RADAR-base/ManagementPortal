package org.radarbase.management.config

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Properties specific to JHipster.
 *
 *
 *  Properties are configured in the application.yml file.
 */
@ConfigurationProperties(prefix = "application", ignoreUnknownFields = false)
class ApplicationProperties
