package org.radarbase.management.service.dto

import org.radarbase.management.domain.enumeration.ComparisonOperator
import org.radarbase.management.domain.enumeration.QueryMetric
import org.radarbase.management.domain.enumeration.QueryTimeFrame

class QueryDTO {
    var metric: QueryMetric? = null // e.g., "heart_rate"

    var operator: ComparisonOperator? = null // e.g., ">"

    var value: String? = null // e.g., 60.0

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
