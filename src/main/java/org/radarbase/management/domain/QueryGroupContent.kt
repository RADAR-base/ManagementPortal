package org.radarbase.management.domain

import org.radarbase.management.domain.enumeration.ContentType
import org.radarbase.management.domain.support.AbstractEntityListener
import java.io.Serializable
import java.time.ZonedDateTime
import javax.persistence.*


@Entity
@Table(name = "query_group_content")
@EntityListeners(
    AbstractEntityListener::class
)
class QueryGroupContent : AbstractEntity(), Serializable{
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator", initialValue = 1000, sequenceName = "hibernate_sequence")
    override var id: Long? = null


    @Column(name = "content_group_name")
    var content_group_name: String? = null

    @ManyToOne
    @JoinColumn(unique = true, name = "query_content_id")
    var queryContent: QueryContent? = null

    @ManyToOne
    @JoinColumn(unique = true, name = "query_group_id")
    var queryGroup: QueryGroup? = null

    @JvmField
    @Column(name = "created_date")
    var createdDate: ZonedDateTime? = null

    @JvmField
    @Column(name = "updated_date")
    var updatedDate: ZonedDateTime? = null

    override fun toString(): String {
        return ("QueryContent{"
                + "content group name='" + content_group_name + '\''
                + ", query group name='" + queryGroup?.name + '\''
                + ", created date='" + createdDate + '\''
                + ", updated date='" + updatedDate + '\''
                + "}")
    }

    companion object {
        private const val serialVersionUID = 1L
    }
}
