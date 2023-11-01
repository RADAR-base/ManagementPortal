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
import org.radarbase.management.domain.support.AbstractEntityListener
import org.radarbase.management.security.Constants
import java.io.Serializable
import java.time.ZonedDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EntityListeners
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.JoinTable
import javax.persistence.ManyToMany
import javax.persistence.SequenceGenerator
import javax.persistence.Table
import javax.validation.constraints.Email
import javax.validation.constraints.NotNull
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size

/**
 * A user.
 */
@Entity
@Audited
@Table(name = "radar_user")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@EntityListeners(
    AbstractEntityListener::class
)
class User : AbstractEntity(), Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator", initialValue = 1000, sequenceName = "hibernate_sequence")
    override var id: Long? = null

    @Column(length = 50, unique = true, nullable = false)
    @NotNull @Pattern(regexp = Constants.ENTITY_ID_REGEX) @Size(min = 1, max = 50) lateinit var login: String
        private set

    @JvmField
    @JsonIgnore
    @Column(name = "password_hash", length = 60)
    @NotNull @Size(min = 60, max = 60) var password: String? = null

    @JvmField
    @Column(name = "first_name", length = 50)
    @Size(max = 50) var firstName: String? = null

    @JvmField
    @Column(name = "last_name", length = 50)
    @Size(max = 50) var lastName: String? = null

    @JvmField
    @Column(length = 100, unique = true, nullable = true)
    @Email @Size(min = 5, max = 100) var email: String? = null

    @JvmField
    @Column(nullable = false)
    @NotNull var activated: Boolean = false

    @JvmField
    @Column(name = "lang_key", length = 5)
    @Size(min = 2, max = 5) var langKey: String? = null

    @JvmField
    @Column(name = "activation_key", length = 20)
    @JsonIgnore
    @Size(max = 20) var activationKey: String? = null

    @JvmField
    @Column(name = "reset_key", length = 20)
    @Size(max = 20) var resetKey: String? = null

    @JvmField
    @Column(name = "reset_date")
    var resetDate: ZonedDateTime? = null

    @JvmField
    @set:JsonSetter(nulls = Nulls.AS_EMPTY)
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "role_users",
        joinColumns = [JoinColumn(name = "users_id", referencedColumnName = "id")],
        inverseJoinColumns = [JoinColumn(name = "roles_id", referencedColumnName = "id")]
    )
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @BatchSize(size = 20)
    @Cascade(
        CascadeType.SAVE_UPDATE
    )
    //TODO remove ?
    var roles: MutableSet<Role>? = HashSet()

    //Lowercase the login before saving it in database
    fun setLogin(login: String) {
        this.login = login.lowercase()
    }

    val authorities: Set<Authority?>?
        /** Authorities that a user has.  */
        get() = roles?.map { obj: Role? -> obj?.authority }?.toSet()

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || javaClass != other.javaClass) {
            return false
        }
        val user = other as User
        return login == user.login
    }

    override fun hashCode(): Int {
        return login.hashCode()
    }

    override fun toString(): String {
        return ("User{"
                + "login='" + login + '\''
                + ", firstName='" + firstName + '\''
                + ", lastName='" + lastName + '\''
                + ", email='" + email + '\''
                + ", activated='" + activated + '\''
                + ", langKey='" + langKey + '\''
                + ", activationKey='" + activationKey + '\''
                + "}")
    }

    companion object {
        private const val serialVersionUID = 1L
    }
}
