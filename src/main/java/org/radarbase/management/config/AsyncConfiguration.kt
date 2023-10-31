package org.radarbase.management.config

import org.slf4j.LoggerFactory
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.AsyncConfigurer
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import tech.jhipster.async.ExceptionHandlingAsyncTaskExecutor
import tech.jhipster.config.JHipsterProperties

@Configuration
@EnableAsync
@EnableScheduling
open class AsyncConfiguration(
    @Autowired private val jHipsterProperties: JHipsterProperties) : AsyncConfigurer {
    @Bean(name = ["taskExecutor"])
    override fun getAsyncExecutor(): ExceptionHandlingAsyncTaskExecutor {
        log.debug("Creating Async Task Executor")
        val executor = ThreadPoolTaskExecutor()
        executor.corePoolSize = jHipsterProperties.async.corePoolSize
        executor.maxPoolSize = jHipsterProperties.async.maxPoolSize
        executor.queueCapacity = jHipsterProperties.async.queueCapacity
        executor.setThreadNamePrefix("management-portal-Executor-")
        return ExceptionHandlingAsyncTaskExecutor(executor)
    }

    override fun getAsyncUncaughtExceptionHandler(): AsyncUncaughtExceptionHandler {
        return SimpleAsyncUncaughtExceptionHandler()
    }

    companion object {
        private val log = LoggerFactory.getLogger(AsyncConfiguration::class.java)
    }
}
