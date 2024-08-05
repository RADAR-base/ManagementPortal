package org.radarbase.management.service.dto

import com.fasterxml.jackson.annotation.JsonInclude
import org.radarbase.management.domain.enumeration.ProjectStatus
import reactor.util.annotation.NonNull
import java.io.Serializable
import javax.validation.constraints.NotNull

@JsonInclude(JsonInclude.Include.NON_NULL)
class PublicProjectDTO : Serializable {
    @NonNull
    var projectName: String? = null

    @NonNull
    var description: String? = null

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    var sourceTypes: Set<SourceTypeDTO> = emptySet()


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PublicProjectDTO

        if (projectName != other.projectName) return false
        if (description != other.description) return false
        if (sourceTypes != other.sourceTypes) return false

        return true
    }

    override fun hashCode(): Int {
        var result = projectName?.hashCode() ?: 0
        result = 31 * result + (description?.hashCode() ?: 0)
        result = 31 * result + sourceTypes.hashCode()
        return result
    }

    override fun toString(): String =
        "PublicProjectDTO(projectName=$projectName, description=$description, sourceTypes=$sourceTypes)"


    companion object {
        private const val serialVersionUID = 1L
    }
}
