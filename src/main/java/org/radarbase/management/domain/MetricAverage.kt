package org.radarbase.management.domain

import org.radarbase.management.domain.enumeration.AggregationPeriod
import org.radarbase.management.domain.enumeration.AggregationType
import org.radarbase.management.domain.support.AbstractEntityListener
import java.io.Serializable
import java.time.ZonedDateTime
import javax.persistence.*


@Entity
@Table(name = "metric_averages")
@EntityListeners(AbstractEntityListener::class)
class MetricAverage : AbstractEntity(), Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator", initialValue = 1000, sequenceName = "hibernate_sequence")
    override var id: Long? = null

    @ManyToOne
    @JoinColumn(name = "subject_id")
    var subject: Subject? = null

    @Column(name = "metric_name")
    var metric: String? = null

    @Column(name = "value")
    var value: Double? = null


    @Column(name = "text_value")
    var textValue: String? = null

    @Enumerated(EnumType.STRING)
    @Column(name = "aggregation_type")
    var aggregationType: AggregationType? = null

    @Enumerated(EnumType.STRING)
    @Column(name = "aggregation_period")
    var aggregationPeriod: AggregationPeriod? = null

    @Column(name = "period_start_date")
    var startDate: ZonedDateTime? = null

    @Column(name = "period_end_date")
    var endDate: ZonedDateTime? = null

    @Column(name = "created_at")
    var created_at: ZonedDateTime? = null
}
