package org.radarbase.management.domain

import org.radarbase.management.domain.enumeration.ComparisonOperator
import org.radarbase.management.domain.enumeration.QueryMetric
import org.radarbase.management.domain.enumeration.QueryTimeFrame
import org.radarbase.management.domain.support.AbstractEntityListener
import java.io.Serializable
import javax.persistence.*

/**
 * Query.
 */
@Entity
@Table(name = "query")
@EntityListeners(AbstractEntityListener::class)
class Query : AbstractEntity(), Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator", initialValue = 1000, sequenceName = "hibernate_sequence")
    override var id: Long? = null

    @ManyToOne
    @JoinColumn(name = "query_group_id")
    var queryGroup: QueryGroup? = null

    @Enumerated(EnumType.STRING)
    @Column(name = "query_metric")
    var queryMetric: QueryMetric? = null

    @Enumerated(EnumType.STRING)
    @Column(name = "comparison_operator")
    var comparisonOperator: ComparisonOperator? = null

    @Column(name = "value")
    var value: String? = null

    @Enumerated(EnumType.STRING)
    @Column(name = "time_frame")
    var timeFrame: QueryTimeFrame? = null

    companion object {
        private const val serialVersionUID = 1L
    }
}
