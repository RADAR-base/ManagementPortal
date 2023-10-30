package org.radarbase.management.service.dto

import com.fasterxml.jackson.annotation.JsonInclude
import java.io.Serializable
import java.util.*
import javax.validation.constraints.NotNull

/**
 * A DTO for the Organization entity.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
class OrganizationDTO : Serializable {
    var id: Long? = null
    lateinit var name: @NotNull String
    lateinit var description: @NotNull String
    lateinit var location: @NotNull String
    var projects: List<ProjectDTO>? = null
    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || javaClass != other.javaClass) {
            return false
        }
        val orgDto = other as OrganizationDTO
        return name == orgDto.name && description == orgDto.description && location == orgDto.location
    }

    override fun hashCode(): Int {
        return Objects.hashCode(name)
    }

    override fun toString(): String {
        return ("OrganizationDTO{"
                + "id=" + id
                + ", name='" + name + "'"
                + ", description='" + description + "'"
                + ", location='" + location + "'"
                + '}')
    }

    companion object {
        private const val serialVersionUID = 1L
    }
}
