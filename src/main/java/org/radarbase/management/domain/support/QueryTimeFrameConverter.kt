package org.radarbase.management.domain.support


import org.radarbase.management.domain.enumeration.ComparisonOperator
import org.radarbase.management.domain.enumeration.QueryTimeFrame
import javax.persistence.AttributeConverter
import javax.persistence.Converter

@Converter(autoApply = false)
class QueryTimeFrameConverter : AttributeConverter<QueryTimeFrame, String> {

    override fun convertToDatabaseColumn(attribute: QueryTimeFrame?): String? {
        return attribute?.symbol.toString()
    }

    override fun convertToEntityAttribute(dbData: String?): QueryTimeFrame? {
        return dbData?.let { symbol ->
            QueryTimeFrame.fromSymbol(symbol)
        }
    }
}
