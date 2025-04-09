package org.radarbase.management.domain.enumeration

enum class QueryTimeFrame(val symbol: String) {
    LAST_7_DAYS("7"),
    PAST_MONTH("30"),
    PAST_6_MONTH("180"),
    PAST_YEAR("365"),
}
