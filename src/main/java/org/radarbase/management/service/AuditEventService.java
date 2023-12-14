package org.radarbase.management.service;

import org.radarbase.management.config.audit.AuditEventConverter;
import org.radarbase.management.repository.PersistenceAuditEventRepository;
import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Service for managing audit events. <p> This is the default implementation to support SpringBoot
 * Actuator AuditEventRepository </p>
 */
@Service
@Transactional
public class AuditEventService {

    private final PersistenceAuditEventRepository persistenceAuditEventRepository;

    private final AuditEventConverter auditEventConverter;

    public AuditEventService(
            PersistenceAuditEventRepository persistenceAuditEventRepository,
            AuditEventConverter auditEventConverter) {
        this.persistenceAuditEventRepository = persistenceAuditEventRepository;
        this.auditEventConverter = auditEventConverter;
    }

    public Page<AuditEvent> findAll(Pageable pageable) {
        return persistenceAuditEventRepository.findAll(pageable)
                .map(auditEventConverter::convertToAuditEvent);
    }

    /**
     * Find audit events by dates.
     *
     * @param fromDate start of the date range
     * @param toDate end of the date range
     * @param pageable the pageable
     * @return a page of audit events
     */
    public Page<AuditEvent> findByDates(LocalDateTime fromDate, LocalDateTime toDate,
            Pageable pageable) {
        return persistenceAuditEventRepository
                .findAllByAuditEventDateBetween(fromDate, toDate, pageable)
                .map(auditEventConverter::convertToAuditEvent);
    }

    public Optional<AuditEvent> find(Long id) {
        return persistenceAuditEventRepository.findById(id)
                .map(auditEventConverter::convertToAuditEvent);
    }
}
