package org.radarbase.management.service.dto

import org.radarbase.management.domain.Query
import org.radarbase.management.domain.enumeration.ComparisonOperator
import org.radarbase.management.domain.enumeration.QueryTimeFrame

class QueryDTO {
    constructor(query: Query?) {
        this.field = query?.field;
        this.operator = query?.operator
        this.value = query?.value
        this.timeFrame = query?.timeFrame
        this.entity = query?.entity
    }

    constructor(metric: String?, operator: ComparisonOperator?, value: String?, timeFrame: QueryTimeFrame?, entity: String? ) {
        this.field = metric.toString();
        this.operator = operator
        this.value = value
        this.timeFrame = timeFrame
        this.entity = entity
    }



    var entity: String? = null
    var field: String? = null
    var operator: ComparisonOperator? = null
    var value: String? = null
    var timeFrame : QueryTimeFrame? = null

    override fun toString(): String {
        return ("QueryDTO{"
                + "metric='" + field + '\''
                + ", operator='" + operator + '\''
                + ", value='" + value + '\''
                + ", time_frame='" + timeFrame + '\''
                + "}")
    }
}
