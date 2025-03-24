package org.radarbase.management.domain


import org.hibernate.annotations.Cascade
import org.hibernate.annotations.CascadeType
import org.hibernate.envers.Audited
import org.radarbase.management.domain.enumeration.ComparisonOperator
import org.radarbase.management.domain.enumeration.QueryMetric
import org.radarbase.management.domain.enumeration.QueryTimeFrame
import org.radarbase.management.domain.support.AbstractEntityListener
import java.io.Serializable
import javax.persistence.*
import javax.validation.constraints.Size

/**
 * A user.
 */
@Entity
@Table(name = "query")
@EntityListeners(
    AbstractEntityListener::class
)
class Query: AbstractEntity(), Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator", initialValue = 1000, sequenceName = "hibernate_sequence")
    override var id: Long? = null

    @JvmField
    @ManyToOne
    @JoinColumn(unique = true, name = "query_group_id")
    @Cascade(CascadeType.PERSIST)
    var queryGroup: QueryGroup? = null


    @JvmField
    @Column(name = "query_metric")
     var queryMetric: QueryMetric? = null;

    @JvmField
    @Column(name = "comparison_operator")
    var comparisonOperator: ComparisonOperator? = null;

    @JvmField
    @Column(name = "value")
     var value: String? = null;


    @JvmField
    @Column(name = "time_frame")
     var timeFrame: QueryTimeFrame? = null;

    override fun toString(): String {
        return ("Query{"
                + "queryGroupName='" + queryGroup?.name + '\''
                + ", queryMetric='" + queryMetric + '\''
                + ", comparisonOperator='" + comparisonOperator + '\''
                + ", value='" + value + '\''
                + ", timeFrame='" + timeFrame + '\''
                + "}")
    }

    companion object {
        private const val serialVersionUID = 1L
    }
}
