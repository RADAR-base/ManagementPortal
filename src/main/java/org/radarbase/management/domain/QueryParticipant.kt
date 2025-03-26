package org.radarbase.management.domain


import org.hibernate.annotations.Cascade
import org.hibernate.annotations.CascadeType
import org.hibernate.envers.Audited
import org.radarbase.management.domain.enumeration.ComparisonOperator
import org.radarbase.management.domain.enumeration.QueryMetric
import org.radarbase.management.domain.enumeration.QueryTimeFrame
import org.radarbase.management.domain.support.AbstractEntityListener
import java.io.Serializable
import java.time.ZonedDateTime
import javax.persistence.*
import javax.validation.constraints.Size


@Entity
@Table(name = "query_participant")
@EntityListeners(
        AbstractEntityListener::class
)
class QueryParticipant: AbstractEntity(), Serializable {
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
    @ManyToOne
    @JoinColumn(unique = true, name = "subject_id")
    @Cascade(CascadeType.PERSIST)
    var subject: Subject? =null

    @JvmField
    @Column(name = "created_date")
    var createdDate: ZonedDateTime? = null

    @JvmField
    @Column(name = "updated_date")
    var updatedDate: ZonedDateTime? = null


    @JvmField
    @ManyToOne( fetch = FetchType.LAZY)
    @JoinColumn(unique = true, name = "created_by")
    var createdBy: User? = null


    override fun toString(): String {
        return ("QueryParticipant{"
                + "id='" + id + '\''
                + "queryGroupId='" + queryGroup + '\''
                + ", createdBy='" + createdBy + '\''
                + ", createdDate='" + createdDate + '\''
                + ", updatedDate='" + updatedDate + '\''
                + "}")
    }

    companion object {
        private const val serialVersionUID = 1L
    }
}
