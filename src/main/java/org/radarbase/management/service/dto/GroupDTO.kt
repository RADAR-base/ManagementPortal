package org.radarbase.management.service.dto

import com.fasterxml.jackson.annotation.JsonInclude
import java.util.*
import javax.validation.constraints.NotNull

/**
 * A DTO for the Group entity.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
class GroupDTO {
    var id: Long? = null
    var projectId: Long? = null
    @NotNull var name: String? = null
    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || javaClass != other.javaClass) {
            return false
        }
        val groupDto = other as GroupDTO
        return if (id == null || groupDto.id == null) {
            false
        } else id == groupDto.id
    }

    override fun hashCode(): Int {
        return Objects.hashCode(id)
    }

    override fun toString(): String {
        return ("GroupDTO{"
                + "id=" + id
                + ", name='" + name + "'"
                + '}')
    }
}
