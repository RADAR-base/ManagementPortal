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
import javax.persistence.*
import javax.validation.constraints.Email
import javax.validation.constraints.NotNull
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size

/**
 * A user.
 */
@Entity
@Table(name = "query_group")
@EntityListeners(
    AbstractEntityListener::class
)
class QueryGroup : AbstractEntity(), Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator", initialValue = 1000, sequenceName = "hibernate_sequence")
    override var id: Long? = null

    @JvmField
    @Column(name = "name")
  var name: String? = null ;

    @JvmField
    @Column(name = "description")
 var description: String? = null;


    @JvmField
    @ManyToOne
    @JoinColumn(unique = true, name = "created_by")

    var createdBy: User? = null


    @JvmField
    @ManyToOne( fetch = FetchType.LAZY)
    @JoinColumn(unique = true, name = "updated_by")
    var updateBy: User? = null

    @JvmField
    @Column(name = "created_date")
    var createdDate: ZonedDateTime? = null

    @JvmField
    @Column(name = "updated_date")
    var updatedDate: ZonedDateTime? = null

//    @JvmField
//    @JsonSetter(nulls = Nulls.AS_EMPTY)
//    @ManyToMany(fetch = FetchType.EAGER)
//    @JoinTable(
//        name = "role_users",
//        joinColumns = [JoinColumn(name = "users_id", referencedColumnName = "id")],
//        inverseJoinColumns = [JoinColumn(name = "roles_id", referencedColumnName = "id")]
//    )




    override fun toString(): String {
        return ("QueryGroup{"
                + "id='" + id + '\''
                + "name='" + name + '\''
                + ", description='" + description + '\''
                + ", createdBy='" + createdBy + '\''
                + ", updateBy='" + updateBy + '\''
                + ", createdDate='" + createdDate + '\''
                + ", updatedDate='" + updatedDate + '\''
                + "}")
    }

    companion object {
        private const val serialVersionUID = 1L
    }
}
