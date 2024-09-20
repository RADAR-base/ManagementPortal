package org.radarbase.management.service

import org.radarbase.management.config.audit.AuditEventConverter
import org.radarbase.management.domain.PersistentAuditEvent
import org.radarbase.management.repository.PersistenceAuditEventRepository
import org.springframework.boot.actuate.audit.AuditEvent
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

/**
 * Service for managing audit events.
 *
 * This is the default implementation to support SpringBoot
 * Actuator AuditEventRepository
 */
@Service
@Transactional
class AuditEventService(
    private val persistenceAuditEventRepository: PersistenceAuditEventRepository,
    private val auditEventConverter: AuditEventConverter,
) {
    fun findAll(pageable: Pageable): Page<AuditEvent> =
        persistenceAuditEventRepository
            .findAll(pageable)
            .map { persistentAuditEvent: PersistentAuditEvent? ->
                auditEventConverter.convertToAuditEvent(
                    persistentAuditEvent!!,
                )
            }

    /**
     * Find audit events by dates.
     *
     * @param fromDate start of the date range
     * @param toDate end of the date range
     * @param pageable the pageable
     * @return a page of audit events
     */
    fun findByDates(
        fromDate: LocalDateTime?,
        toDate: LocalDateTime?,
        pageable: Pageable?,
    ): Page<AuditEvent> =
        persistenceAuditEventRepository
            .findAllByAuditEventDateBetween(fromDate, toDate, pageable)
            .map { persistentAuditEvent: PersistentAuditEvent? ->
                auditEventConverter.convertToAuditEvent(
                    persistentAuditEvent!!,
                )
            }

    fun find(id: Long): Optional<AuditEvent> =
        persistenceAuditEventRepository
            .findById(id)
            .map { persistentAuditEvent: PersistentAuditEvent? ->
                auditEventConverter.convertToAuditEvent(
                    persistentAuditEvent!!,
                )
            }
}
