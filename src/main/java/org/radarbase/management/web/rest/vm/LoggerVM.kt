package org.radarbase.management.web.rest.vm

import ch.qos.logback.classic.Logger
import com.fasterxml.jackson.annotation.JsonCreator

/**
 * View Model object for storing a Logback logger.
 */
class LoggerVM {
    var name: String? = null
    var level: String? = null

    constructor(logger: Logger) {
        name = logger.name
        level = logger.effectiveLevel.toString()
    }

    @JsonCreator
    constructor()

    override fun toString(): String =
        (
            "LoggerVM{" +
                "name='" + name + '\'' +
                ", level='" + level + '\'' +
                '}'
            )
}
