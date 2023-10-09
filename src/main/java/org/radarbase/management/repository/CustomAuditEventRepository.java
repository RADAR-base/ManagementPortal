package org.radarbase.management.repository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import ch.qos.logback.classic.pattern.TargetLengthBasedClassNameAbbreviator;
import org.radarbase.management.config.audit.AuditEventConverter;
import org.radarbase.management.domain.PersistentAuditEvent;
import org.radarbase.management.security.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.boot.actuate.audit.AuditEventRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * An implementation of Spring Boot's AuditEventRepository.
 */
@Repository
public class CustomAuditEventRepository implements AuditEventRepository {
    private static final Logger logger = LoggerFactory.getLogger(CustomAuditEventRepository.class);

    private static final String AUTHORIZATION_FAILURE = "AUTHORIZATION_FAILURE";

    private static final TargetLengthBasedClassNameAbbreviator TYPE_ABBREVIATOR =
            new TargetLengthBasedClassNameAbbreviator(15);

    @Autowired
    private PersistenceAuditEventRepository persistenceAuditEventRepository;

    @Autowired
    private AuditEventConverter auditEventConverter;

    @Override
    public List<AuditEvent> find(String principal, Instant after, String type) {
        Iterable<PersistentAuditEvent> persistentAuditEvents =
                persistenceAuditEventRepository
                        .findByPrincipalAndAuditEventDateAfterAndAuditEventType(principal,
                                LocalDateTime.from(after), type);
        return auditEventConverter.convertToAuditEvent(persistentAuditEvents);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void add(AuditEvent event) {
        var eventType = event.getType();
        if (!AUTHORIZATION_FAILURE.equals(eventType)
                && !Constants.ANONYMOUS_USER.equals(event.getPrincipal())) {
            PersistentAuditEvent persistentAuditEvent = new PersistentAuditEvent();
            persistentAuditEvent.setPrincipal(event.getPrincipal());
            persistentAuditEvent.setAuditEventType(eventType);
            persistentAuditEvent.setAuditEventDate(LocalDateTime.ofInstant(event.getTimestamp(),
                    ZoneId.systemDefault()));
            persistentAuditEvent.setData(auditEventConverter.convertDataToStrings(event.getData()));
            persistenceAuditEventRepository.save(persistentAuditEvent);
        }
        if (eventType != null && eventType.endsWith("_FAILURE")) {
            Object typeObj = event.getData().get("type");
            String errorType = typeObj instanceof String
                    ? TYPE_ABBREVIATOR.abbreviate((String) typeObj)
                    : null;
            logger.warn("{}: principal={}, error={}, message=\"{}\", details={}",
                    eventType,
                    event.getPrincipal(),
                    errorType,
                    event.getData().get("message"),
                    event.getData().get("details"));
        }
    }
}
