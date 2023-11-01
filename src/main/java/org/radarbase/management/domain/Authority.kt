package org.radarbase.management.domain

import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import org.hibernate.envers.Audited
import org.radarbase.auth.authorization.RoleAuthority
import org.radarbase.management.security.Constants
import java.io.Serializable
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table
import javax.validation.constraints.NotNull
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size

/**
 * An authority (a security role) used by Spring Security.
 */
@Entity
@Audited
@Table(name = "radar_authority")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
data class Authority(@JvmField
                     @Id
                     @Column(length = 50)
                     @NotNull @Size(min = 0, max = 50) @Pattern(regexp = Constants.ENTITY_ID_REGEX) var name: String? = null) : Serializable {

    constructor(role: RoleAuthority) : this(role.authority)

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || javaClass != other.javaClass) {
            return false
        }
        val authority = other as Authority
        return if (name == null || authority.name == null) {
            false
        } else name == authority.name
    }

    override fun hashCode(): Int {
        return if (name != null) name.hashCode() else 0
    }

    override fun toString(): String {
        return name.toString()
    }

    companion object {
        private const val serialVersionUID = 1L
    }

    fun asString() : String? {
        return name
    }
}
