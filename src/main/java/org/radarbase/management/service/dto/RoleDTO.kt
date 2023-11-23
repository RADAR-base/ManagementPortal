package org.radarbase.management.service.dto

import com.fasterxml.jackson.annotation.JsonInclude

/**
 * Created by nivethika on 23-5-17.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
class RoleDTO {
    var id: Long? = null
    var organizationId: Long? = null
    var organizationName: String? = null
    var projectId: Long? = null
    var projectName: String? = null
    var authorityName: String? = null
    override fun toString(): String {
        return ("RoleDTO{" + "id=" + id
                + ", organizationId=" + organizationId
                + ", projectId=" + projectId
                + ", authorityName='" + authorityName + '\''
                + '}')
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || javaClass != other.javaClass) {
            return false
        }
        val roleDto = other as RoleDTO
        return id == roleDto.id && organizationId == roleDto.organizationId && organizationName == roleDto.organizationName && projectId == roleDto.projectId && projectName == roleDto.projectName && authorityName == roleDto.authorityName
    }

    override fun hashCode(): Int {
        return if (id != null) id.hashCode() else 0
    }
}
