package org.radarbase.management.service.dto

import com.fasterxml.jackson.annotation.JsonInclude
import org.radarbase.management.domain.enumeration.ProjectStatus
import reactor.util.annotation.NonNull
import java.io.Serializable
import javax.validation.constraints.NotNull

@JsonInclude(JsonInclude.Include.NON_NULL)
class ProjectLiteDTO : Serializable {
    @NonNull
    var projectName: String? = null

    @NonNull
    var description: String? = null

    @NotNull
    var location: String? = null
    var projectStatus: ProjectStatus? = null


    override fun toString() = "PublicProjectDTO(" +
            "projectName=$projectName, " +
            "description=$description, " +
            "location=$location, " +
            "projectStatus=$projectStatus" +
            ")"


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ProjectLiteDTO

        if (projectName != other.projectName) return false
        if (description != other.description) return false
        if (location != other.location) return false
        if (projectStatus != other.projectStatus) return false

        return true
    }

    override fun hashCode(): Int {
        var result = projectName?.hashCode() ?: 0
        result = 31 * result + (description?.hashCode() ?: 0)
        result = 31 * result + (location?.hashCode() ?: 0)
        result = 31 * result + (projectStatus?.hashCode() ?: 0)
        return result
    }

    companion object {
        private const val serialVersionUID = 1L
    }
}
