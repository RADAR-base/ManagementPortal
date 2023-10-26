package org.radarbase.management.domain

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.annotation.Nulls
import org.hibernate.annotations.BatchSize
import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import org.hibernate.annotations.Cascade
import org.hibernate.annotations.CascadeType
import org.hibernate.envers.Audited
import org.hibernate.envers.RelationTargetAuditMode
import org.radarbase.auth.authorization.RoleAuthority
import org.radarbase.management.domain.support.AbstractEntityListener
import java.io.Serializable
import java.time.LocalDate
import java.time.ZonedDateTime
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
import javax.persistence.OneToMany
import javax.persistence.OneToOne
import javax.persistence.SequenceGenerator
import javax.persistence.Table
import javax.validation.constraints.NotNull

/**
 * A Subject.
 */
@Entity
@Audited
@Table(name = "subject")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@EntityListeners(
    AbstractEntityListener::class
)
class Subject(
    @Id @GeneratedValue(
        strategy = GenerationType.SEQUENCE,
        generator = "sequenceGenerator"
    ) @SequenceGenerator(
        name = "sequenceGenerator",
        initialValue = 1000,
        sequenceName = "hibernate_sequence"
    ) override var id: Long? = null
) : AbstractEntity(), Serializable {

    @JvmField
    @Column(name = "external_link")
    var externalLink: String? = null

    @JvmField
    @Column(name = "external_id")
    var externalId: String? = null

    @Column(name = "removed", nullable = false)
    var isRemoved: @NotNull Boolean? = false

    @JvmField
    @OneToOne
    @JoinColumn(unique = true, name = "user_id")
    @Cascade(CascadeType.ALL)
    var user: User? = null

    @JvmField
    @OneToMany(mappedBy = "subject", fetch = FetchType.LAZY)
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @Cascade(
        CascadeType.SAVE_UPDATE
    )
    var sources: MutableSet<Source> = HashSet()

    @JvmField
    @set:JsonSetter(nulls = Nulls.AS_EMPTY)
    @ElementCollection(fetch = FetchType.EAGER)
    @MapKeyColumn(name = "attribute_key")
    @Column(name = "attribute_value")
    @CollectionTable(name = "subject_metadata", joinColumns = [JoinColumn(name = "id")])
    @Cascade(
        CascadeType.ALL
    )
    @BatchSize(size = 50)
    var attributes: Map<String, String> = HashMap()

    @OneToMany(mappedBy = "subject", orphanRemoval = true, fetch = FetchType.LAZY)
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @JsonIgnore
    val metaTokens: Set<MetaToken> = HashSet()

    @JvmField
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "group_id")
    @Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
    var group: Group? = null

    @JvmField
    @Column(name = "date_of_birth")
    var dateOfBirth: LocalDate? = null

    @JvmField
    @Column(name = "enrollment_date")
    var enrollmentDate: ZonedDateTime? = null

    @JvmField
    @Column(name = "person_name")
    var personName: String? = null
    fun externalLink(externalLink: String?): Subject {
        this.externalLink = externalLink
        return this
    }

    fun externalId(enternalId: String?): Subject {
        externalId = enternalId
        return this
    }

    fun removed(removed: Boolean?): Subject {
        isRemoved = removed
        return this
    }

    fun user(usr: User?): Subject {
        user = usr
        return this
    }

    fun sources(sources: MutableSet<Source>): Subject {
        this.sources = sources
        return this
    }

    val activeProject: Project?
        /**
         * Gets the active project of subject.
         *
         *
         *  There can be only one role with PARTICIPANT authority
         * and the project that is related to that role is the active role.
         *
         * @return [Project] currently active project of subject.
         */
        get() = user?.roles
            ?.first { r -> r.authority?.name == RoleAuthority.PARTICIPANT.authority }
            ?.project
    val associatedProject: Project?
        /**
         * Get the active project of a subject, and otherwise the
         * inactive project.
         * @return the project a subject belongs to, if any.
         */
        get() {
            val user = user ?: return null
            return user.roles?.asIterable()
                ?.filter { r -> PARTICIPANT_TYPES.contains(r.authority?.name) }
                ?.sortedBy { it.authority?.name }?.first()
                .let { obj: Role? -> obj?.project }

        }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || javaClass != other.javaClass) {
            return false
        }
        val subject = other as Subject
        return if (subject.id == null || id == null) {
            false
        } else id == subject.id
    }

    override fun hashCode(): Int {
        return Objects.hashCode(id)
    }

    override fun toString(): String {
        return ("Subject{"
                + "id=" + id
                + ", externalLink='" + externalLink + '\''
                + ", externalId='" + externalId + '\''
                + ", removed=" + isRemoved
                + ", user=" + user
                + ", sources=" + sources
                + ", attributes=" + attributes
                + ", group=" + group
                + "}")
    }

    companion object {
        private const val serialVersionUID = 1L
        private val PARTICIPANT_TYPES = java.util.Set.of(
            RoleAuthority.PARTICIPANT.authority,
            RoleAuthority.INACTIVE_PARTICIPANT.authority
        )
    }
}
