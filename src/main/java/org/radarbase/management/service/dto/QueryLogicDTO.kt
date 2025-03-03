package org.radarbase.management.service.dto

import org.radarbase.management.domain.enumeration.QueryLogicOperator

class QueryLogicDTO {
    var queryGroupId: Long? = null
    var logicOperator: QueryLogicOperator? = null
    var query: QueryDTO?  = null
    var children: List<QueryLogicDTO>? = null


    override fun toString(): String {
        return ("QueryLogicDTO{"
                + "queryGroupId='" + queryGroupId + '\''
                + ", logicOperator='" + logicOperator + '\''
                + ", query='" + query + '\''
                + ", children='" + children + '\''
                + "}")
    }
}
