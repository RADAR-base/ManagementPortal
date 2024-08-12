package org.radarbase.management.service.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.annotation.Nulls
import java.io.Serializable
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.*
import org.radarbase.auth.kratos.KratosSessionDTO

/**
 * A DTO for the Subject entity.
 */
class KratosSubjectWebhookDTO : Serializable {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    var identity: KratosSessionDTO.Identity? = null

    @JsonInclude(JsonInclude.Include.NON_NULL)
    var payload: Map<String, String>? = null

    @JsonInclude(JsonInclude.Include.NON_NULL)
    var cookies: Map<String, String>? = null

    companion object {
        private const val serialVersionUID = 1L
    }
}
