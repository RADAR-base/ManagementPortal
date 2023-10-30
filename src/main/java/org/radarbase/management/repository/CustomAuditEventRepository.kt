package org.radarbase.management.repository

import ch.qos.logback.classic.pattern.TargetLengthBasedClassNameAbbreviator
import org.radarbase.management.config.audit.AuditEventConverter
import org.radarbase.management.domain.PersistentAuditEvent
import org.radarbase.management.security.Constants
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.actuate.audit.AuditEvent
import org.springframework.boot.actuate.audit.AuditEventRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * An implementation of Spring Boot's AuditEventRepository.
 */
@Repository
class CustomAuditEventRepository(
    @Autowired private val auditEventConverter: AuditEventConverter,
    @Autowired private val persistenceAuditEventRepository: PersistenceAuditEventRepository
) : AuditEventRepository {

    override fun find(principal: String, after: Instant, type: String): List<AuditEvent> {
        val persistentAuditEvents: Iterable<PersistentAuditEvent>? = persistenceAuditEventRepository
            ?.findByPrincipalAndAuditEventDateAfterAndAuditEventType(
                principal,
                LocalDateTime.from(after), type
            )
        return auditEventConverter.convertToAuditEvent(persistentAuditEvents)
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    override fun add(event: AuditEvent) {
        val eventType = event.type
        if (AUTHORIZATION_FAILURE != eventType
            && Constants.ANONYMOUS_USER != event.principal
        ) {
            val persistentAuditEvent = PersistentAuditEvent()
            persistentAuditEvent.principal = event.principal
            persistentAuditEvent.auditEventType = eventType
            persistentAuditEvent.auditEventDate = LocalDateTime.ofInstant(
                event.timestamp,
                ZoneId.systemDefault()
            )
            persistentAuditEvent.data = auditEventConverter!!.convertDataToStrings(event.data)
            persistenceAuditEventRepository!!.save(persistentAuditEvent)
        }
        if (eventType != null && eventType.endsWith("_FAILURE")) {
            val typeObj = event.data["type"]
            val errorType = if (typeObj is String) TYPE_ABBREVIATOR.abbreviate(typeObj as String?) else null
            logger.warn(
                "{}: principal={}, error={}, message=\"{}\", details={}",
                eventType,
                event.principal,
                errorType,
                event.data["message"],
                event.data["details"]
            )
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(CustomAuditEventRepository::class.java)
        private const val AUTHORIZATION_FAILURE = "AUTHORIZATION_FAILURE"
        private val TYPE_ABBREVIATOR = TargetLengthBasedClassNameAbbreviator(15)
    }
}
