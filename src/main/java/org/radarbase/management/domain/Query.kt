package org.radarbase.management.domain

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import org.radarbase.management.domain.enumeration.ComparisonOperator
import org.radarbase.management.domain.enumeration.QueryTimeFrame
import org.radarbase.management.domain.support.AbstractEntityListener
import org.radarbase.management.domain.support.ComparisonOperatorConverter
import org.radarbase.management.domain.support.QueryTimeFrameConverter
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
    @JsonIgnore
    override var id: Long? = null

    @ManyToOne
    @JoinColumn(name = "query_group_id")
    @JsonIgnore
    var queryGroup: QueryGroup? = null

    @Column(name = "entity")
    var entity: String? = null

    //RP-469 currently unclear how many items and what items we will have, changing this to string temporarily
    // once we are deviced on this (based on the next Sandra feedback) we can provide a more robust solution
    //  @Enumerated(EnumType.STRING)
    @Column(name = "query_metric")
    var field: String? = null

    @Convert(converter = ComparisonOperatorConverter::class)
    @Column(name = "comparison_operator")
    var operator: ComparisonOperator? = null

    @Column(name = "value")
    var value: String? = null

    @Convert(converter = QueryTimeFrameConverter::class)
    @Column(name = "time_frame")
    var timeFrame: QueryTimeFrame? = null

    override fun toString(): String {return ("Query{"
            + "queryGroupName='" + queryGroup?.name + '\''
            + ", entity='" + entity + '\''
            + ", queryMetric='" + field + '\''
            + ", comparisonOperator='" + operator + '\''
            + ", value='" + value + '\''
            + ", timeFrame='" + timeFrame + '\''
            + "}")
    }

    companion object {
        private const val serialVersionUID = 1L
    }
}
