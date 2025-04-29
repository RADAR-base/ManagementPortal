package org.radarbase.management.service.dto

import org.radarbase.management.domain.Query
import org.radarbase.management.domain.enumeration.ComparisonOperator
import org.radarbase.management.domain.enumeration.QueryMetric
import org.radarbase.management.domain.enumeration.QueryTimeFrame

class QueryDTO {
    constructor(query: Query?) {
        this.metric = query?.queryMetric;
        this.operator = query?.comparisonOperator
        this.value = query?.value
        this.time_frame = query?.timeFrame
    }

    constructor(metric: QueryMetric?, operator: ComparisonOperator?, value: String?, timeFrame: QueryTimeFrame?) {
        this.metric = metric;
        this.operator = operator
        this.value = value
        this.time_frame = timeFrame
    }

    var metric: QueryMetric? = null
    var operator: ComparisonOperator? = null
    var value: String? = null
    var time_frame : QueryTimeFrame? = null

    override fun toString(): String {
        return ("QueryDTO{"
                + "metric='" + metric + '\''
                + ", operator='" + operator + '\''
                + ", value='" + value + '\''
                + ", time_frame='" + time_frame + '\''
                + "}")
    }
}
