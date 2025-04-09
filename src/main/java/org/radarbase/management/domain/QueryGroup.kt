package org.radarbase.management.domain

import javax.persistence.CascadeType
import org.radarbase.management.domain.support.AbstractEntityListener
import java.io.Serializable
import java.time.ZonedDateTime
import javax.persistence.*

/**
 * Query Group.
 */
@Entity
@Table(name = "query_group")
@EntityListeners(AbstractEntityListener::class)
class QueryGroup : AbstractEntity(), Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator", initialValue = 1000, sequenceName = "hibernate_sequence")
    override var id: Long? = null

    @Column(name = "name")
    var name: String? = null

    @Column(name = "description")
    var description: String? = null

    @ManyToOne
    @JoinColumn(name = "created_by")
    var createdBy: User? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    var updateBy: User? = null

    @Column(name = "created_date")
    var createdDate: ZonedDateTime? = null

    @Column(name = "updated_date")
    var updatedDate: ZonedDateTime? = null

    @OneToMany(mappedBy = "queryGroup", cascade = [CascadeType.ALL], orphanRemoval = true)
    var queries: MutableList<Query> = mutableListOf()

    @OneToMany(mappedBy = "queryGroup", cascade = [CascadeType.ALL], orphanRemoval = true)
    var queryParticipants: MutableList<QueryParticipant> = mutableListOf()

    @OneToMany(mappedBy = "queryGroup", cascade = [CascadeType.ALL], orphanRemoval = true)
    var queryLogics: MutableList<QueryLogic> = mutableListOf()

    companion object {
        private const val serialVersionUID = 1L
    }
}
