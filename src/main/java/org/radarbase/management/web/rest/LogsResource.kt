package org.radarbase.management.web.rest

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.LoggerContext
import io.micrometer.core.annotation.Timed
import org.radarbase.auth.authorization.RoleAuthority
import org.radarbase.management.web.rest.vm.LoggerVM
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.security.access.annotation.Secured
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

/**
 * Controller for view and managing Log Level at runtime.
 */
@RestController
@RequestMapping("/management")
class LogsResource {
    @get:Secured(RoleAuthority.SYS_ADMIN_AUTHORITY)
    @get:Timed
    @get:GetMapping("/logs")
    val list: List<LoggerVM>
        /**
         * Returns all the logger configurations from current logger context.
         * @return the logger configurations
         */
        get() {
            val context = LoggerFactory.getILoggerFactory() as LoggerContext
            return context.getLoggerList()
                .stream()
                .map { logger: Logger -> LoggerVM(logger) }
                .toList()
        }

    /**
     * Changes logger level.
     * @param jsonLogger param
     */
    @PutMapping("/logs")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Timed
    @Secured(RoleAuthority.SYS_ADMIN_AUTHORITY)
    fun changeLevel(@RequestBody jsonLogger: LoggerVM) {
        val context = LoggerFactory.getILoggerFactory() as LoggerContext
        context.getLogger(jsonLogger.name).setLevel(Level.valueOf(jsonLogger.level))
    }
}
