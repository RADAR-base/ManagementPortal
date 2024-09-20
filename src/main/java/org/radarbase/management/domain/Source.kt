package org.radarbase.management.domain

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.annotation.Nulls
import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import org.hibernate.envers.Audited
import org.radarbase.management.domain.support.AbstractEntityListener
import org.radarbase.management.security.Constants
import java.io.Serializable
import java.util.*
import javax.persistence.CollectionTable
import javax.persistence.Column
import javax.persistence.ElementCollection
import javax.persistence.Entity
import javax.persistence.EntityListeners
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.MapKeyColumn
import javax.persistence.PrePersist
import javax.persistence.SequenceGenerator
import javax.persistence.Table
import javax.validation.constraints.NotNull
import javax.validation.constraints.Pattern

/**
 * A Source.
 */
@Entity
@Audited
@Table(name = "radar_source")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@EntityListeners(
    AbstractEntityListener::class,
)
class Source :
    AbstractEntity,
    Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator", initialValue = 1000, sequenceName = "hibernate_sequence")
    override var id: Long? = null

    // pass
    @JvmField
    @Column(name = "source_id", nullable = false, unique = true)
    @NotNull
    var sourceId: UUID? = null

    @JvmField
    @Column(name = "source_name", nullable = false, unique = true)
    @NotNull
    @Pattern(regexp = Constants.ENTITY_ID_REGEX)
    var sourceName: String? = null

    @JvmField
    @Column(name = "expected_source_name")
    var expectedSourceName: String? = null

    @Column(name = "assigned", nullable = false)
    @NotNull
    var assigned: Boolean? = false

    @Column(name = "deleted", nullable = false)
    @NotNull
    var deleted: Boolean = false

    @JvmField
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "subject_id")
    @JsonIgnore
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    var subject: Subject? = null

    @JvmField
    @ManyToOne(fetch = FetchType.EAGER)
    var sourceType: SourceType? = null

    @JvmField
    @ManyToOne(fetch = FetchType.LAZY)
    var project: Project? = null

    @JvmField
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    @ElementCollection(fetch = FetchType.EAGER)
    @MapKeyColumn(name = "attribute_key")
    @Column(name = "attribute_value")
    @CollectionTable(name = "source_metadata", joinColumns = [JoinColumn(name = "id")])
    var attributes: Map<String, String> = HashMap()

    /**
     * Default constructor. Needed for other JPA operations.
     */
    constructor()

    /**
     * Constructor with SourceType. This will assign sourceType and assign default values for
     * sourceId and sourceName.
     * @param sourceType sourceType of the source.
     */
    constructor(sourceType: SourceType?) {
        this.sourceType = sourceType
        generateUuid()
    }

    fun sourceId(devicePhysicalId: UUID?): Source {
        sourceId = devicePhysicalId
        return this
    }

    /**
     * Add default values for sourceId and sourceName if they are not provided before persisting
     * this object. The default for sourceId is to generate a new UUID. The default for
     * sourceName is to take to model name, and append a dash followed by the first 8 characters
     * of the string representation of the UUID.
     */
    @PrePersist
    fun generateUuid() {
        if (sourceId == null) {
            sourceId = UUID.randomUUID()
        }
        if (sourceName == null) {
            sourceName =
                java.lang.String.join(
                    "-",
                    sourceType?.model,
                    sourceId.toString().substring(0, 8),
                )
        }
    }

    fun sourceType(sourceType: SourceType?): Source {
        this.sourceType = sourceType
        return this
    }

    fun project(project: Project?): Source {
        this.project = project
        return this
    }

    fun subject(subject: Subject?): Source {
        this.subject = subject
        return this
    }

    fun sourceName(sourceName: String?): Source {
        this.sourceName = sourceName
        return this
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || javaClass != other.javaClass) {
            return false
        }
        val source = other as Source
        return if (source.id == null || id == null) {
            false
        } else {
            id == source.id && sourceId == source.sourceId
        }
    }

    override fun hashCode(): Int = Objects.hash(id, sourceId)

    override fun toString(): String =
        (
            "Source{" +
                "id=" + id +
                ", sourceId='" + sourceId + '\'' +
                ", sourceName='" + sourceName + '\'' +
                ", assigned=" + assigned +
                ", sourceType=" + sourceType +
                ", project=" + project +
                '}'
            )

    companion object {
        private const val serialVersionUID = 1L
    }
}
