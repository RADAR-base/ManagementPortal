package org.radarbase.management.service.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.annotation.Nulls
import java.io.Serializable
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.*

/**
 * A DTO for the Subject entity.
 */
class KratosSubjectWebhookDTO : Serializable {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    var identity_id: String? = null

    @JsonInclude(JsonInclude.Include.NON_NULL)
    var flow_id: String? = null

    @JsonInclude(JsonInclude.Include.NON_NULL)
    var session_token: String? = null

    @JsonInclude(JsonInclude.Include.NON_NULL)
    var project_id: String? = null

    companion object {
        private const val serialVersionUID = 1L
    }
}
