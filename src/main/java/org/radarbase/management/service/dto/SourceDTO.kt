package org.radarbase.management.service.dto

import com.fasterxml.jackson.annotation.JsonInclude
import java.io.Serializable
import java.util.*
import javax.validation.constraints.NotNull

/**
 * A DTO for the Source entity.
 */
class SourceDTO : Serializable {
    var id: Long? = null
    var sourceId: UUID? = null
    lateinit var sourceName: @NotNull String
    var expectedSourceName: String? = null
    var assigned: @NotNull Boolean? = null
    lateinit var sourceType: @NotNull SourceTypeDTO
    var subjectLogin: String? = null

    @JsonInclude(JsonInclude.Include.NON_NULL)
    var project: MinimalProjectDetailsDTO? = null
    var attributes: Map<String, String>? = null
    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || javaClass != other.javaClass) {
            return false
        }
        val source = other as SourceDTO
        return id == source.id && sourceId == source.sourceId && sourceName == source.sourceName && expectedSourceName == source.expectedSourceName && assigned == source.assigned && sourceType == source.sourceType && subjectLogin == source.subjectLogin && project == source.project && attributes == source.attributes
    }

    override fun hashCode(): Int {
        return Objects.hash(
            id, sourceId, sourceName, expectedSourceName, assigned, sourceType,
            subjectLogin, project, attributes
        )
    }

    override fun toString(): String {
        return ("SourceDTO{"
                + "id=" + id
                + ", sourceId='" + sourceId + '\''
                + ", sourceName='" + sourceName + '\''
                + ", assigned=" + assigned
                + ", sourceType=" + sourceType
                + ", project=" + project
                + ", subjectLogin=" + subjectLogin
                + '}')
    }

    companion object {
        private const val serialVersionUID = 1L
    }
}
