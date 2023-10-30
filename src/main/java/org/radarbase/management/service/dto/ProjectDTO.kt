package org.radarbase.management.service.dto

import com.fasterxml.jackson.annotation.JsonInclude
import org.radarbase.management.domain.enumeration.ProjectStatus
import java.io.Serializable
import java.time.ZonedDateTime
import java.util.*
import javax.validation.constraints.NotNull

/**
 * A DTO for the Project entity.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
class ProjectDTO : Serializable {
    var id: Long? = null
    var projectName: @NotNull String? = null
    var humanReadableProjectName: String? = null
    var description: @NotNull String? = null
    var organization: OrganizationDTO? = null
    var organizationName: String? = null
    var location: @NotNull String? = null
    var startDate: ZonedDateTime? = null
    var projectStatus: ProjectStatus? = null
    var endDate: ZonedDateTime? = null

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    var sourceTypes: Set<SourceTypeDTO>? = null
    var attributes: Map<String, String>? = null

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    var groups: Set<GroupDTO>? = null
    var persistentTokenTimeout: Long? = null
    override fun equals(o: Any?): Boolean {
        if (this === o) {
            return true
        }
        if (o == null || javaClass != o.javaClass) {
            return false
        }
        val projectDto = o as ProjectDTO
        return if (id == null || projectDto.id == null) {
            false
        } else id == projectDto.id
    }

    override fun hashCode(): Int {
        return Objects.hashCode(id)
    }

    override fun toString(): String {
        return ("ProjectDTO{"
                + "id=" + id
                + ", projectName='" + projectName + "'"
                + ", description='" + description + "'"
                + ", organization='" + organization + "'"
                + ", organizationName='" + organizationName + "'"
                + ", location='" + location + "'"
                + ", startDate='" + startDate + "'"
                + ", projectStatus='" + projectStatus + "'"
                + ", endDate='" + endDate + "'"
                + '}')
    }

    companion object {
        private const val serialVersionUID = 1L
        const val EXTERNAL_PROJECT_URL_KEY = "External-project-url"
        const val EXTERNAL_PROJECT_ID_KEY = "External-project-id"
        const val WORK_PACKAGE_KEY = "Work-package"
        const val PHASE_KEY = "Phase"
        const val HUMAN_READABLE_PROJECT_NAME = "Human-readable-project-name"
        const val PRIVACY_POLICY_URL = "Privacy-policy-url"
    }
}
