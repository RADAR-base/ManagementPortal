package org.radarbase.management.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.format.datetime.standard.DateTimeFormatterRegistrar;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Nonnull;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.ResolverStyle;

import static java.time.temporal.ChronoField.HOUR_OF_DAY;
import static java.time.temporal.ChronoField.MINUTE_OF_HOUR;
import static java.time.temporal.ChronoField.NANO_OF_SECOND;
import static java.time.temporal.ChronoField.SECOND_OF_MINUTE;

@Configuration
public class DateTimeFormatConfiguration implements WebMvcConfigurer {

    @Override
    public void addFormatters(@Nonnull FormatterRegistry registry) {
        DateTimeFormatterRegistrar registrar = new DateTimeFormatterRegistrar();
        registrar.setUseIsoFormat(true);
        registrar.setDateTimeFormatter(new DateTimeFormatterBuilder()
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
                .parseDefaulting(HOUR_OF_DAY, HOUR_OF_DAY.range().getMinimum())
                .parseDefaulting(MINUTE_OF_HOUR, MINUTE_OF_HOUR.range().getMinimum())
                .parseDefaulting(SECOND_OF_MINUTE, SECOND_OF_MINUTE.range().getMinimum())
                .parseDefaulting(NANO_OF_SECOND, NANO_OF_SECOND.range().getMinimum())
                .toFormatter()
                .withZone(ZoneId.of("UTC"))
                .withResolverStyle(ResolverStyle.LENIENT));
        registrar.registerFormatters(registry);
    }
}
