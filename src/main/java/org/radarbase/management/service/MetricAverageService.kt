package org.radarbase.management.service

import org.radarbase.management.domain.MetricAverage
import org.radarbase.management.domain.Subject
import org.radarbase.management.domain.enumeration.AggregationPeriod
import org.radarbase.management.domain.enumeration.AggregationType
import org.radarbase.management.repository.MetricAveragesRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime


@Service
@Transactional
class MetricAverageService(
   private val subjectService: SubjectService,
   private val metricAveragesRepository: MetricAveragesRepository
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun addNewAverageMetricEntry(
        jsonData: S3JsonData,
        feature: String,
        mean: Double?,
        stringValue: String?,
        aggregationType: AggregationType,
        aggregationPeriod: AggregationPeriod
    ) {


        val startDateStr = jsonData.data_summary.start_date
        val endDateStr = jsonData.data_summary.end_date

        if(startDateStr.isNullOrBlank() || endDateStr.isNullOrBlank()) {
            log.info("[MetricAverageService] the start date or end date is null")
            return
        }


        val subject = subjectService.findOneByLogin(jsonData.patient_id)
        val startDate = parseDateAtStartOfDay(jsonData.data_summary.start_date)
        val endDate = parseDateAtStartOfDay(jsonData.data_summary.end_date)
        val roundedMean = mean?.let { "%.2f".format(it).toDouble() }

        val existingAverages = metricAveragesRepository.findAllBySubjectAndStartDateAndMetric(subject, startDate, feature)

        if (existingAverages.isEmpty()) {
            val newAverage = buildMetricAverage(
                feature = feature,
                value = roundedMean,
                textValue = stringValue,
                startDate = startDate,
                endDate = endDate,
                aggregationType = aggregationType,
                aggregationPeriod = aggregationPeriod,
                subject = subject
            )
            metricAveragesRepository.save(newAverage)
        } else {
            val existing = existingAverages.first()

            val needsUpdate = when(aggregationType) {
                AggregationType.MAX -> existing.textValue != stringValue
                else -> existing.value != roundedMean
            }

            if(needsUpdate) {

                when(aggregationType) {
                    AggregationType.MAX -> existing.textValue = stringValue
                    else -> existing.value = roundedMean
                }

                existing.created_at = ZonedDateTime.now();
                metricAveragesRepository.save(existing);
            }
        }
    }

    private fun parseDateAtStartOfDay(date: String?): ZonedDateTime =
        LocalDate.parse(date).atStartOfDay(ZoneId.of("UTC"))

    private fun buildMetricAverage(
        feature: String,
        value: Double?,
        textValue: String?,
        startDate: ZonedDateTime,
        endDate: ZonedDateTime,
        aggregationType: AggregationType,
        aggregationPeriod: AggregationPeriod,
        subject: Subject
    ): MetricAverage {
        return MetricAverage().apply {
            this.metric = feature
            this.value = if (aggregationType != AggregationType.MAX) value else null
            this.textValue = if (aggregationType == AggregationType.MAX) textValue else null
            this.startDate = startDate
            this.endDate = endDate
            this.created_at = ZonedDateTime.now()
            this.aggregationType = aggregationType
            this.aggregationPeriod = aggregationPeriod
            this.subject = subject
        }
    }



}

