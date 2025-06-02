package org.radarbase.management.service.dto

import org.radarbase.management.domain.enumeration.QueryLogicOperator
import org.radarbase.management.domain.enumeration.QueryLogicType

class QueryLogicDTO {
    var queryGroupId: Long? = null
    var logic_operator: QueryLogicOperator? = null
    var query: QueryDTO?  = null
    var children: List<QueryLogicDTO>? = null
    var type: QueryLogicType? = null

    override fun toString(): String {
        return ("QueryLogicDTO{"
                + "queryGroupId='" + queryGroupId + '\''
                + ", logic_operator='" + logic_operator + '\''
                + ", query='" + query + '\''
                + ", children='" + children + '\''
                + ", type='" + type + '\''
                + "}")
    }
}
