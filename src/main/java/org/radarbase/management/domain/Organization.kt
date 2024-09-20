package org.radarbase.management.domain

import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import org.hibernate.envers.Audited
import org.radarbase.management.domain.support.AbstractEntityListener
import org.radarbase.management.security.Constants
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EntityListeners
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.OneToMany
import javax.persistence.SequenceGenerator
import javax.persistence.Table
import javax.validation.constraints.NotNull
import javax.validation.constraints.Pattern

/**
 * An Organization.
 */
@Entity
@Audited
@Table(name = "radar_organization")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@EntityListeners(
    AbstractEntityListener::class,
)
class Organization : AbstractEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator", initialValue = 1000, sequenceName = "hibernate_sequence")
    override var id: Long? = null

    @JvmField
    @Column(name = "name", nullable = false, unique = true)
    @NotNull
    @Pattern(regexp = Constants.ENTITY_ID_REGEX)
    var name: String? = null

    @JvmField
    @Column(name = "description", nullable = false)
    @NotNull
    var description: String? = null

    @JvmField
    @Column(name = "location", nullable = false)
    @NotNull
    var location: String? = null

    @JvmField
    @OneToMany(mappedBy = "organization")
    var projects: List<Project> = emptyList()

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || javaClass != other.javaClass) {
            return false
        }
        val org = other as Organization
        return if (org.id == null || id == null) {
            false
        } else {
            id == org.id
        }
    }

    override fun hashCode(): Int = Objects.hashCode(id)

    override fun toString(): String =
        (
            "Organization{" +
                "id=" + id +
                ", name='" + name + "'" +
                ", description='" + description + "'" +
                ", location='" + location + "'" +
                "}"
            )

    companion object {
        private const val serialVersionUID = 1L
    }
}
