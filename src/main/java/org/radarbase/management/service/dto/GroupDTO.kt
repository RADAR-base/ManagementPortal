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
    var name: @NotNull String? = null
    override fun equals(o: Any?): Boolean {
        if (this === o) {
            return true
        }
        if (o == null || javaClass != o.javaClass) {
            return false
        }
        val groupDto = o as GroupDTO
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