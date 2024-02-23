package org.radarbase.management.repository

import org.radarbase.management.domain.PersistentAuditEvent
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDateTime

/**
 * Spring Data JPA repository for the PersistentAuditEvent entity.
 */
interface PersistenceAuditEventRepository : JpaRepository<PersistentAuditEvent?, Long?> {
    fun findByPrincipal(principal: String?): List<PersistentAuditEvent?>?
    fun findByAuditEventDateAfter(after: LocalDateTime?): List<PersistentAuditEvent?>?
    fun findByPrincipalAndAuditEventDateAfter(
        principal: String?,
        after: LocalDateTime?
    ): List<PersistentAuditEvent?>?

    fun findByPrincipalAndAuditEventDateAfterAndAuditEventType(
        principle: String?, after: LocalDateTime?, type: String?
    ): List<PersistentAuditEvent>

    fun findAllByAuditEventDateBetween(
        fromDate: LocalDateTime?,
        toDate: LocalDateTime?, pageable: Pageable?
    ): Page<PersistentAuditEvent>
}
