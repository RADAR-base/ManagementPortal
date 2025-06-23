package org.radarbase.management.repository
import org.radarbase.management.domain.MetricAverage
import org.radarbase.management.domain.Subject
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.RepositoryDefinition
import java.time.ZonedDateTime


@Suppress("unused")
@RepositoryDefinition(domainClass = MetricAverage::class, idClass = Long::class)
interface MetricAveragesRepository : JpaRepository<MetricAverage, Long>  {
    fun findAllBySubjectAndStartDateAndMetric(subject: Subject, startDate: ZonedDateTime, metric: String): List<MetricAverage>
}
