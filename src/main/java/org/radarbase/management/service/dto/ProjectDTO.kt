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

    @NotNull
    var projectName: String? = null
    var humanReadableProjectName: String? = null

    @NotNull
    var description: String? = null
    var organization: OrganizationDTO? = null

    // Defaults to organization name, but if that is not set then we can use the organizationName
    var organizationName: String? = null
        get() {
            if (organization?.name != null) {
                field = organization?.name
            }
            return field
        }

    @NotNull
    var location: String? = null
    var startDate: ZonedDateTime? = null
    var projectStatus: ProjectStatus? = null
    var endDate: ZonedDateTime? = null

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    var sourceTypes: Set<SourceTypeDTO> = emptySet()
    var attributes: Map<String, String>? = null

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    var groups: Set<GroupDTO>? = null
    var persistentTokenTimeout: Long? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || javaClass != other.javaClass) {
            return false
        }
        val projectDto = other as ProjectDTO
        return if (id == null || projectDto.id == null) {
            false
        } else {
            id == projectDto.id
        }
    }

    override fun hashCode(): Int = Objects.hashCode(id)

    override fun toString(): String =
        (
            "ProjectDTO{" +
                "id=" + id +
                ", projectName='" + projectName + "'" +
                ", description='" + description + "'" +
                ", organization='" + organization + "'" +
                ", organizationName='" + organizationName + "'" +
                ", location='" + location + "'" +
                ", startDate='" + startDate + "'" +
                ", projectStatus='" + projectStatus + "'" +
                ", endDate='" + endDate + "'" +
                '}'
            )

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
