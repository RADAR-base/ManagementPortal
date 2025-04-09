package org.radarbase.management.domain

import org.radarbase.management.domain.support.AbstractEntityListener
import java.io.Serializable
import java.time.ZonedDateTime
import javax.persistence.*


@Entity
@Table(name = "query_evaluation")
@EntityListeners(
    AbstractEntityListener::class
)
class QueryEvaluation: AbstractEntity(), Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator", initialValue = 1000, sequenceName = "hibernate_sequence")
    override var id: Long? = null

    @JvmField
    @ManyToOne
    @JoinColumn(unique = true, name = "query_group_id")
    var queryGroup: QueryGroup? = null

    @JvmField
    @OneToOne
    @JoinColumn(unique = true, name = "subject_id")
    var subject: Subject? = null

    @JvmField
    @Column(name = "created_date")
    var createdDate: ZonedDateTime? = null

    @JvmField
    @Column(name = "updated_date")
    var updatedDate: ZonedDateTime? = null

    @JvmField
    @Column(name = "result")
    var result: Boolean? = null;

    override fun toString(): String {
        return ("QueryEvaluation{"
                + "queryGroupName='" + queryGroup?.name + '\''
                + ", subject='" + subject + '\''
                + ", created date='" + createdDate + '\''
                + ", updated date='" + updatedDate + '\''
                + ", result='" + result + '\''
                + "}")
    }

    companion object {
        private const val serialVersionUID = 1L
    }
}
