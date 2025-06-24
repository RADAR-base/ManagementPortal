package org.radarbase.management.domain

import org.radarbase.management.domain.enumeration.ContentType
import org.radarbase.management.domain.support.AbstractEntityListener
import java.io.Serializable
import javax.persistence.*


@Entity
@Table(name = "query_content")
@EntityListeners(
    AbstractEntityListener::class
)
class QueryContent : AbstractEntity(), Serializable {




    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator", initialValue = 1000, sequenceName = "hibernate_sequence")
    override var id: Long? = null


    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    public var type: ContentType? = null


    @Column(name = "heading")
    public var heading: String? = null

    @Column(name = "text_value")
    public var value: String? = null


    @Column(name = "image")
    public var imageBlob: ByteArray? = null


    @Column(name = "image_alternative_text")
    public val imageAltText: String? = null

    @ManyToOne
    @JoinColumn(unique = true, name = "query_group_id")
    var queryGroup: QueryGroup? = null

    @ManyToOne
    @JoinColumn(unique = true, name = "query_content_group_id")
    var queryContentGroup: QueryContentGroup? = null


    override fun toString(): String {
        return ("QueryContent{"
                + "type='" + type + '\''
                + ", value='" + value + '\''
                + ", image alt text='" + imageAltText + '\''
                + "}")
    }

    companion object {
        private const val serialVersionUID = 1L
    }
}
