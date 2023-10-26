package org.radarbase.management.domain

import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.annotation.Nulls
import java.io.Serializable
import java.time.LocalDateTime
import javax.persistence.CollectionTable
import javax.persistence.Column
import javax.persistence.ElementCollection
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.MapKeyColumn
import javax.persistence.SequenceGenerator
import javax.persistence.Table
import javax.validation.constraints.NotNull

/**
 * Persist AuditEvent managed by the Spring Boot actuator.
 *
 * @see org.springframework.boot.actuate.audit.AuditEvent
 */
@Entity
@Table(name = "jhi_persistent_audit_event")
class PersistentAuditEvent : Serializable {
    @JvmField
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator", initialValue = 1000, sequenceName = "hibernate_sequence")
    @Column(name = "event_id")
    var id: Long? = null

    @JvmField
    @Column(nullable = false)
    var principal: @NotNull String? = null

    @JvmField
    @Column(name = "event_date")
    var auditEventDate: LocalDateTime? = null

    @JvmField
    @Column(name = "event_type")
    var auditEventType: String? = null

    @set:JsonSetter(nulls = Nulls.AS_EMPTY)
    @ElementCollection
    @MapKeyColumn(name = "name")
    @Column(name = "value")
    @CollectionTable(name = "jhi_persistent_audit_evt_data", joinColumns = [JoinColumn(name = "event_id")])
    var data: Map<String, String> = HashMap()

    companion object {
        private const val serialVersionUID = 1L
    }
}
