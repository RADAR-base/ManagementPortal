package org.radarbase.management.service.dto

import org.radarbase.management.domain.enumeration.QueryLogicOperator
import org.radarbase.management.domain.enumeration.QueryLogicType

class AngularQueryBuilderDTO {
    var queryGroupName: String? = null
    var queryGroupDescription: String? = null

    var condition: String? = null

    var field: String? = null
    var operator: String? = null
    var value: String? = null
    var timeFame : String? = null

    var rules: List<AngularQueryBuilderDTO>? = null
    var type: QueryLogicType? = null

    override fun toString(): String {
        return ("QueryLogicDTO{"
                + "queryGroupName='" + queryGroupName + '\''
                + "queryGroupDescription='" + queryGroupDescription + '\''
                + ", operator='" + operator + '\''

                + ", field='" + field + '\''
                + ", operator='" + operator + '\''
                + ", value='" + value + '\''
                + ", timeFrame='" + timeFame + '\''

                + ", rules='" + rules + '\''
                + ", type='" + type + '\''
                + "}")
    }
}
