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
class SubjectDTO : Serializable {
    enum class SubjectStatus {
        DEACTIVATED,

        // activated = false, removed = false
        ACTIVATED,

        // activated = true,  removed = false
        DISCONTINUED,

        // activated = false, removed = true
        INVALID // activated = true,  removed = true (invalid state, makes no sense)
    }

    var id: Long? = null
    private var _login: String? = null
    var login: String?
        get() = {
            if (_login == null) {
                _login = UUID.randomUUID().toString()
                _login
            } else {
                _login
            }
        }.toString()
        set(value) {
            _login = value
        }

    var externalLink: String? = null
    var externalId: String? = null
    var status = SubjectStatus.DEACTIVATED

    @JsonInclude(JsonInclude.Include.NON_NULL)
    var createdBy: String? = null

    @JsonInclude(JsonInclude.Include.NON_NULL)
    var createdDate: ZonedDateTime? = null

    @JsonInclude(JsonInclude.Include.NON_NULL)
    var lastModifiedBy: String? = null

    @JsonInclude(JsonInclude.Include.NON_NULL)
    var lastModifiedDate: ZonedDateTime? = null

    @JsonInclude(JsonInclude.Include.NON_NULL)
    var project: ProjectDTO? = null
    var group: String? = null
    var dateOfBirth: LocalDate? = null
    var enrollmentDate: ZonedDateTime? = null
    var personName: String? = null

    @set:JsonSetter(nulls = Nulls.AS_EMPTY)
    var roles: List<RoleDTO> = ArrayList()
    var sources: Set<MinimalSourceDetailsDTO> = HashSet()
    var attributes: Map<String, String> = HashMap()

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || javaClass != other.javaClass) {
            return false
        }
        val subjectDto = other as SubjectDTO
        return if (id == null || subjectDto.id == null) {
            false
        } else id != subjectDto.id
    }

    override fun hashCode(): Int {
        return Objects.hashCode(id)
    }

    override fun toString(): String {
        return ("SubjectDTO{" + "id=" + id + ", login='" + login + '\'' + ", externalLink='" + externalLink + '\'' + ", externalId='" + externalId + '\'' + ", status=" + status + ", project=" + (project?.projectName) + ", attributes=" + attributes + '}')
    }

    companion object {
        private const val serialVersionUID = 1L
        const val HUMAN_READABLE_IDENTIFIER_KEY = "Human-readable-identifier"
    }
}
