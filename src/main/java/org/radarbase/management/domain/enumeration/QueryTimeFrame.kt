package org.radarbase.management.domain.enumeration

enum class QueryTimeFrame(val symbol: Int) {
    LAST_7_DAYS(7),
    PAST_MONTH(30),
    PAST_6_MONTH(180),
    PAST_YEAR(365);

    companion object {
        fun fromSymbol(symbol: Int): QueryTimeFrame {
            return values().find { it.symbol == symbol }
                ?: throw IllegalArgumentException("[ComparisonOperator] Unknown symbol: $symbol")
        }
    }
}
