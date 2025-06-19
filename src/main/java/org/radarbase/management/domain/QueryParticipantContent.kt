package org.radarbase.management.domain


import org.radarbase.management.domain.support.AbstractEntityListener
import java.io.Serializable
import java.time.ZonedDateTime
import javax.persistence.*



@Entity
@Table(name = "query_participant_content")
@EntityListeners(AbstractEntityListener::class)
class QueryParticipantContent : AbstractEntity(), Serializable {

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

    @ManyToOne
    @JoinColumn(name = "query_content_group_id")
    var queryContentGroup: QueryContentGroup? = null


    @Column(name = "created_date")
    var createdDate: ZonedDateTime? = null

    @Column(name = "is_archived")
    var isArchived: Boolean? = null
}
