package org.radarbase.management.service.dto

import org.radarbase.management.domain.enumeration.ComparisonOperator
import org.radarbase.management.domain.enumeration.QueryMetric
import org.radarbase.management.domain.enumeration.QueryTimeFrame

class QueryDTO {
    var queryMetric: QueryMetric? = null // e.g., "heart_rate"

    var comparisonOperator: ComparisonOperator? = null // e.g., ">"

    var value: String? = null // e.g., 60.0

    var timeFrame : QueryTimeFrame? = null

    override fun toString(): String {
        return ("QueryDTO{"
                + "queryMetric='" + queryMetric + '\''
                + ", comparisonOperator='" + comparisonOperator + '\''
                + ", value='" + value + '\''
                + ", timeFrame='" + timeFrame + '\''
                + "}")
    }
}
