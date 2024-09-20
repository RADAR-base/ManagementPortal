package org.radarbase.management.config

import org.springframework.context.annotation.Configuration
import org.springframework.format.FormatterRegistry
import org.springframework.format.datetime.standard.DateTimeFormatterRegistrar
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.format.ResolverStyle
import java.time.temporal.ChronoField
import javax.annotation.Nonnull

@Configuration
class DateTimeFormatConfiguration : WebMvcConfigurer {
    override fun addFormatters(
        @Nonnull registry: FormatterRegistry,
    ) {
        val registrar = DateTimeFormatterRegistrar()
        registrar.setUseIsoFormat(true)
        registrar.setDateTimeFormatter(
            DateTimeFormatterBuilder()
                .parseCaseInsensitive()
                .append(DateTimeFormatter.ISO_LOCAL_DATE)
                .optionalStart()
                .appendLiteral('T')
                .append(DateTimeFormatter.ISO_LOCAL_TIME)
                .optionalStart()
                .parseLenient()
                .appendOffsetId()
                .parseStrict()
                .optionalEnd()
                .optionalStart()
                .appendLiteral('[')
                .parseCaseSensitive()
                .appendZoneRegionId()
                .appendLiteral(']')
                .optionalEnd()
                .optionalEnd()
                .parseDefaulting(ChronoField.HOUR_OF_DAY, ChronoField.HOUR_OF_DAY.range().minimum)
                .parseDefaulting(ChronoField.MINUTE_OF_HOUR, ChronoField.MINUTE_OF_HOUR.range().minimum)
                .parseDefaulting(ChronoField.SECOND_OF_MINUTE, ChronoField.SECOND_OF_MINUTE.range().minimum)
                .parseDefaulting(ChronoField.NANO_OF_SECOND, ChronoField.NANO_OF_SECOND.range().minimum)
                .toFormatter()
                .withZone(ZoneId.of("UTC"))
                .withResolverStyle(ResolverStyle.LENIENT),
        )
        registrar.registerFormatters(registry)
    }
}
