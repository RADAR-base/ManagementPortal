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
class SubjectWebhookDTO : Serializable, SubjectDTO() {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    val projectName: String? = null

    val emailAddress: String? = null

    companion object {
        private const val serialVersionUID = 1L
    }
}
