package org.radarbase.management.domain

import org.radarbase.management.domain.support.AbstractEntityListener
import java.io.Serializable
import java.time.ZonedDateTime
import javax.persistence.*


@Entity
@Table(name = "query_participant")
@EntityListeners(AbstractEntityListener::class)
class QueryParticipant : AbstractEntity(), Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator", initialValue = 1000, sequenceName = "hibernate_sequence")
    override var id: Long? = null

    @ManyToOne
    @JoinColumn(name = "query_group_id")
    var queryGroup: QueryGroup? = null

    @ManyToOne
    @JoinColumn(name = "subject_id")
    var subject: Subject? = null

    @Column(name = "created_date")
    var createdDate: ZonedDateTime? = null

    @Column(name = "updated_date")
    var updatedDate: ZonedDateTime? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    var createdBy: User? = null

    companion object {
        private const val serialVersionUID = 1L
    }
}
