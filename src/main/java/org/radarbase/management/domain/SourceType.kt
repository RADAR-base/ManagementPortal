package org.radarbase.management.domain

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.annotation.Nulls
import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import org.hibernate.annotations.Cascade
import org.hibernate.annotations.CascadeType
import org.hibernate.envers.Audited
import org.radarbase.management.domain.support.AbstractEntityListener
import org.radarbase.management.security.Constants
import java.io.Serializable
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EntityListeners
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.ManyToMany
import javax.persistence.OneToMany
import javax.persistence.SequenceGenerator
import javax.persistence.Table
import javax.validation.constraints.NotNull
import javax.validation.constraints.Pattern

/**
 * A SourceType.
 */
@Entity
@Audited
@Table(name = "source_type")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@EntityListeners(
    AbstractEntityListener::class
)
class SourceType : AbstractEntity(), Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator", initialValue = 1000, sequenceName = "hibernate_sequence")
    override var id: Long? = null

    @JvmField
    @Column(name = "producer")
    @NotNull @Pattern(regexp = Constants.ENTITY_ID_REGEX) var producer: String? = null

    @JvmField
    @Column(name = "name")
    var name: String? = null

    @JvmField
    @Column(name = "description")
    var description: String? = null

    @JvmField
    @Column(name = "assessment_type")
    var assessmentType: String? = null

    @JvmField
    @Column(name = "app_provider")
    var appProvider: String? = null

    @JvmField
    @Column(name = "model", nullable = false)
    @NotNull @Pattern(regexp = Constants.ENTITY_ID_REGEX) var model: String? = null

    @JvmField
    @Column(name = "catalog_version", nullable = false)
    @NotNull @Pattern(regexp = Constants.ENTITY_ID_REGEX) var catalogVersion: String? = null

    @JvmField
    @Column(name = "source_type_scope", nullable = false)
    @NotNull var sourceTypeScope: String? = null

    @JvmField
    @Column(name = "dynamic_registration", nullable = false)
    @NotNull var canRegisterDynamically: Boolean? = false

    @JvmField
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    @OneToMany(mappedBy = "sourceType", orphanRemoval = true, fetch = FetchType.LAZY)
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @Cascade(
        CascadeType.DELETE, CascadeType.SAVE_UPDATE
    )
    var sourceData: Set<SourceData> = HashSet()

    @set:JsonSetter(nulls = Nulls.AS_EMPTY)
    @ManyToMany(mappedBy = "sourceTypes", fetch = FetchType.LAZY)
    @JsonIgnore
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    var projects: Set<Project> = HashSet()
    fun producer(producer: String?): SourceType {
        this.producer = producer
        return this
    }

    fun model(model: String?): SourceType {
        this.model = model
        return this
    }

    fun catalogVersion(catalogVersion: String?): SourceType {
        this.catalogVersion = catalogVersion
        return this
    }

    fun sourceTypeScope(sourceTypeScope: String?): SourceType {
        this.sourceTypeScope = sourceTypeScope
        return this
    }

    fun sourceData(sourceData: Set<SourceData>): SourceType {
        this.sourceData = sourceData
        return this
    }

    fun projects(projects: Set<Project>): SourceType {
        this.projects = projects
        return this
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || javaClass != other.javaClass) {
            return false
        }
        val sourceType = other as SourceType
        return if (sourceType.id == null || id == null) {
            false
        } else id == sourceType.id && producer == sourceType.producer && model == sourceType.model && catalogVersion == sourceType.catalogVersion && canRegisterDynamically == sourceType.canRegisterDynamically && sourceTypeScope == sourceType.sourceTypeScope && name == sourceType.name && description == sourceType.description && appProvider == sourceType.appProvider && assessmentType == sourceType.assessmentType
    }

    override fun hashCode(): Int {
        return Objects.hash(
            id, model, producer, catalogVersion, canRegisterDynamically,
            sourceTypeScope, name, description, appProvider, assessmentType
        )
    }

    override fun toString(): String {
        return ("SourceType{"
                + "id=" + id
                + ", producer='" + producer + '\''
                + ", model='" + model + '\''
                + ", catalogVersion='" + catalogVersion + '\''
                + ", sourceTypeScope=" + sourceTypeScope
                + ", canRegisterDynamically=" + canRegisterDynamically
                + ", name='" + name + '\''
                + ", description=" + description
                + ", appProvider=" + appProvider
                + ", assessmentType=" + assessmentType
                + '}')
    }

    companion object {
        private const val serialVersionUID = 1L
    }
}
