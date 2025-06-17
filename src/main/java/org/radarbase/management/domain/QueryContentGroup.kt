package org.radarbase.management.domain

import org.radarbase.management.domain.support.AbstractEntityListener
import java.io.Serializable
import java.time.ZonedDateTime
import javax.persistence.*


@Entity
@Table(name = "query_content_group")
@EntityListeners(AbstractEntityListener::class)
class QueryContentGroup : AbstractEntity(), Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator", initialValue = 1000, sequenceName = "hibernate_sequence")
    override var id: Long? = null

    @Column(name = "content_group_name", nullable = false)
    var contentGroupName: String? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "query_content_id", nullable = false)
    var queryContent: QueryContent? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "query_group_id", nullable = false)
    var queryGroup: QueryGroup? = null

    @Column(name = "created_date")
    var createdDate: ZonedDateTime? = null

    @Column(name = "updated_date")
    var updatedDate: ZonedDateTime? = null

    override fun toString(): String {
        return ("QueryContentGroup{"
                + "ContentGroupName='" + contentGroupName + '\''
                + ", queryGroup='" + queryGroup?.name + '\''
                + ", createdDate=" + createdDate
                + ", updatedDate=" + updatedDate
                + "}")
    }

    companion object {
        private const val serialVersionUID = 1L
    }
}
