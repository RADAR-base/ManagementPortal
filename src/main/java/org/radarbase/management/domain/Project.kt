package org.radarbase.management.domain

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.annotation.Nulls
import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import org.hibernate.annotations.Cascade
import org.hibernate.annotations.CascadeType
import org.hibernate.annotations.DynamicInsert
import org.hibernate.envers.Audited
import org.hibernate.envers.NotAudited
import org.radarbase.management.domain.enumeration.ProjectStatus
import org.radarbase.management.domain.support.AbstractEntityListener
import java.io.Serializable
import java.time.ZonedDateTime
import java.util.*
import javax.persistence.CollectionTable
import javax.persistence.Column
import javax.persistence.ElementCollection
import javax.persistence.Entity
import javax.persistence.EntityListeners
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.JoinTable
import javax.persistence.ManyToMany
import javax.persistence.ManyToOne
import javax.persistence.MapKeyColumn
import javax.persistence.OneToMany
import javax.persistence.OrderBy
import javax.persistence.SequenceGenerator
import javax.persistence.Table
import javax.validation.constraints.NotNull
import javax.validation.constraints.Pattern

/**
 * A Project.
 */
@Entity
@Audited
@Table(name = "project")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@EntityListeners(
    AbstractEntityListener::class
)
@DynamicInsert
class Project : AbstractEntity(), Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator", initialValue = 1000, sequenceName = "hibernate_sequence")
    override var id: Long? = null

    @Column(name = "project_name", nullable = false, unique = true)
    @NotNull @Pattern(regexp = "^[_'.@A-Za-z0-9- ]*$") var projectName: String? = null

    @Column(name = "description", nullable = false)
    @NotNull var description: String? = null

    // Defaults to organization name, but if that is not set then we can use the organizationName
    @Column(name = "jhi_organization")
    var organizationName: String? = null
        get() {
            if (organization?.name != null)
                field = organization?.name
            return field
        }
        // needed because the @JVMField annotation cannot be added when a custom getter/setter is set
        set(value) {
            field = value
        }


    @JvmField
    @ManyToOne(fetch = FetchType.EAGER)
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    var organization: Organization? = null

    @JvmField
    @Column(name = "location", nullable = false)
    @NotNull var location: String? = null

    @JvmField
    @Column(name = "start_date")
    var startDate: ZonedDateTime? = null

    @JvmField
    @Enumerated(EnumType.STRING)
    @Column(name = "project_status")
    var projectStatus: ProjectStatus? = null

    @JvmField
    @Column(name = "end_date")
    var endDate: ZonedDateTime? = null

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    @JsonIgnore
    @OneToMany(mappedBy = "project", fetch = FetchType.LAZY)
    @Cascade(CascadeType.ALL)
    var roles: Set<Role> = HashSet()

    @JvmField
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    @ManyToMany(fetch = FetchType.LAZY)
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @JoinTable(
        name = "project_source_type",
        joinColumns = [JoinColumn(name = "projects_id", referencedColumnName = "id")],
        inverseJoinColumns = [JoinColumn(name = "source_types_id", referencedColumnName = "id")]
    )
    var sourceTypes: Set<SourceType> = HashSet()

    @JvmField
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    @ElementCollection(fetch = FetchType.EAGER)
    @MapKeyColumn(name = "attribute_key")
    @Column(name = "attribute_value")
    @CollectionTable(name = "project_metadata", joinColumns = [JoinColumn(name = "id")])
    var attributes: MutableMap<String, String> = HashMap()

    @JvmField
    @NotAudited
    @OneToMany(
        mappedBy = "project",
        fetch = FetchType.LAZY,
        orphanRemoval = true,
        cascade = [javax.persistence.CascadeType.REMOVE, javax.persistence.CascadeType.REFRESH, javax.persistence.CascadeType.DETACH]
    )
    @OrderBy("name ASC")
    var groups: MutableSet<Group> = HashSet()
    fun projectName(projectName: String?): Project {
        this.projectName = projectName
        return this
    }

    fun description(description: String?): Project {
        this.description = description
        return this
    }

    fun organizationName(organizationName: String?): Project {
        this.organizationName = organizationName
        return this
    }

    fun organization(organization: Organization?): Project {
        this.organization = organization
        return this
    }

    fun location(location: String?): Project {
        this.location = location
        return this
    }

    fun startDate(startDate: ZonedDateTime?): Project {
        this.startDate = startDate
        return this
    }

    fun projectStatus(projectStatus: ProjectStatus?): Project {
        this.projectStatus = projectStatus
        return this
    }

    fun endDate(endDate: ZonedDateTime?): Project {
        this.endDate = endDate
        return this
    }

    fun sourceTypes(sourceTypes: Set<SourceType>): Project {
        this.sourceTypes = sourceTypes
        return this
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || javaClass != other.javaClass) {
            return false
        }
        val project = other as Project
        return if (project.id == null || id == null) {
            false
        } else id == project.id
    }

    override fun hashCode(): Int {
        return Objects.hashCode(id)
    }

    override fun toString(): String {
        return ("Project{"
                + "id=" + id
                + ", projectName='" + projectName + "'"
                + ", description='" + description + "'"
                + ", organization='" + organizationName + "'"
                + ", location='" + location + "'"
                + ", startDate='" + startDate + "'"
                + ", projectStatus='" + projectStatus + "'"
                + ", endDate='" + endDate + "'"
                + "}")
    }

    companion object {
        private const val serialVersionUID = 1L
    }
}
