package org.radarbase.management.config

import org.radarbase.management.aop.logging.LoggingAspect
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.EnableAspectJAutoProxy
import org.springframework.context.annotation.Profile
import org.springframework.core.env.Environment
import tech.jhipster.config.JHipsterConstants

@Configuration
@EnableAspectJAutoProxy
open class LoggingAspectConfiguration {
    @Bean
    @Profile(JHipsterConstants.SPRING_PROFILE_DEVELOPMENT)
    open fun loggingAspect(env: Environment?): LoggingAspect {
        return LoggingAspect(env!!)
    }
}
