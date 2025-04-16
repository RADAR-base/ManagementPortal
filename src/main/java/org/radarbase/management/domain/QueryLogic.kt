package org.radarbase.management.domain

import org.radarbase.management.domain.enumeration.*
import org.radarbase.management.domain.support.AbstractEntityListener
import java.io.Serializable
import javax.persistence.*
import javax.validation.constraints.Email
import javax.validation.constraints.NotNull
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size
import kotlin.jvm.Transient

/**
 * Query Logic
 */
@Entity
@Table(name = "query_logic")
@EntityListeners(AbstractEntityListener::class)
class QueryLogic : AbstractEntity(), Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator", initialValue = 1000, sequenceName = "hibernate_sequence")
    override var id: Long? = null

    @ManyToOne
    @JoinColumn(name = "query_group_id")
    var queryGroup: QueryGroup? = null

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    var type: QueryLogicType? = null

    @Enumerated(EnumType.STRING)
    @Column(name = "logic_operator")
    var logicOperator: QueryLogicOperator? = null

    @OneToOne
    @JoinColumn(name = "query_id", unique = true)
    var query: Query? = null

    @ManyToOne
    @JoinColumn(name = "parent_id")
    var parent: QueryLogic? = null

    override fun toString(): String {
        return ("QueryLogic{"
                + "id='" + id + '\''
                + "queryGroupId='" + queryGroup + '\''
                + ", type='" + type + '\''
                + ", logicOperator='" + logicOperator + '\''
                + ", query_id='" + query + '\''
                + ", parent='" + parent + '\''
                + "}")
    }

    @Transient
    var children: MutableList<QueryLogic> = mutableListOf()


    companion object {
        private const val serialVersionUID = 1L
    }
}
