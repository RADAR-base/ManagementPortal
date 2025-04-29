package org.radarbase.management.domain.enumeration

enum class ComparisonOperator(val symbol: String) {
    EQUALS("="),
    NOT_EQUALS("!="),
    GREATER_THAN(">"),
    LESS_THAN("<"),
    GREATER_THAN_OR_EQUALS(">="),
    LESS_THAN_OR_EQUALS("<=");

    fun apply(a: Int, b: Int): Boolean {
        return when (this) {
            EQUALS -> a == b
            NOT_EQUALS -> a != b
            GREATER_THAN -> a > b
            LESS_THAN -> a < b
            GREATER_THAN_OR_EQUALS -> a >= b
            LESS_THAN_OR_EQUALS -> a <= b
        }
    }

    companion object {
        fun fromSymbol(symbol: String): ComparisonOperator {
            return values().find { it.symbol == symbol }
                ?: throw IllegalArgumentException("[ComparisonOperator] Unknown symbol: $symbol")
        }
    }
}
