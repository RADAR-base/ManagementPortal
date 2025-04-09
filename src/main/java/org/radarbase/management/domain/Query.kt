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
    @Cascade(CascadeType.ALL)
    var queryGroup: QueryGroup? = null


    @JvmField
    @Column(name = "query_metric")
    @Enumerated(EnumType.STRING)
     var metric: QueryMetric? = null;

    @JvmField
    @Column(name = "comparison_operator")
    @Enumerated(EnumType.STRING)
    var operator: ComparisonOperator? = null;

    @JvmField
    @Column(name = "value")
     var value: String? = null;


    @JvmField
    @Column(name = "time_frame")
    @Enumerated(EnumType.STRING)
     var time_frame: QueryTimeFrame? = null;

    override fun toString(): String {
        return ("Query{"
                + "queryGroupName='" + queryGroup?.name + '\''
                + ", metric='" + metric + '\''
                + ", operator='" + operator + '\''
                + ", value='" + value + '\''
                + ", time_frame='" + time_frame + '\''
                + "}")
    }

    companion object {
        private const val serialVersionUID = 1L
    }
}
