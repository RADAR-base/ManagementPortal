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
import org.radarbase.management.domain.enumeration.*
import org.radarbase.management.domain.support.AbstractEntityListener
import org.radarbase.management.repository.QueryGroupRepository
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
@Table(name = "query_logic")
@EntityListeners(
    AbstractEntityListener::class
)
class QueryLogic: AbstractEntity(), Serializable  {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator", initialValue = 1000, sequenceName = "hibernate_sequence")
     override var id: Long? = null

    @JvmField
    @ManyToOne
    @JoinColumn(unique = true, name = "query_group_id")
    @Cascade(CascadeType.ALL)
    var queryGroup: QueryGroup? = null

    @JvmField
    @Column(name = "type")
    var type: QueryLogicType? = null;

    @JvmField
    @Column(name = "logic_operator")
   var logic_operator: QueryLogicOperator? = null;

    @JvmField
    @OneToOne
    @JoinColumn(unique = true, name = "query_id")
    @Cascade(CascadeType.ALL)
    var query: Query? = null

    @JvmField
    @OneToOne
    @JoinColumn(unique = true, name = "parent_id")
    @Cascade(CascadeType.ALL)
    var parent: QueryLogic? = null


    companion object {
        private const val serialVersionUID = 1L
    }
}
