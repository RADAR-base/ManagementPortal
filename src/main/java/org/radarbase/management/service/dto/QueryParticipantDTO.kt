package org.radarbase.management.service.dto

import org.radarbase.management.domain.User

class QueryParticipantDTO {

     var queryGroupId: Long? = null
     var subjectId: Long? = null
    var createdBy: UserDTO? = null

    override fun toString(): String {
        return ("QueryParticipantDTO{"
                + "queryGroupId='" + queryGroupId + '\''
                + ", subjectId='" + subjectId + '\''
                + ", createdBy='" + createdBy + '\''
                + "}")
    }

}
