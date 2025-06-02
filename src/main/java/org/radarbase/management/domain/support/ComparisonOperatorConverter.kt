package org.radarbase.management.domain.support


import org.radarbase.management.domain.enumeration.ComparisonOperator
import javax.persistence.AttributeConverter
import javax.persistence.Converter

@Converter(autoApply = false)
class ComparisonOperatorConverter : AttributeConverter<ComparisonOperator, String> {

    override fun convertToDatabaseColumn(attribute: ComparisonOperator?): String? {
        return attribute?.symbol
    }

    override fun convertToEntityAttribute(dbData: String?): ComparisonOperator? {
        return dbData?.let { symbol ->
            ComparisonOperator.fromSymbol(symbol)
        }
    }
}
