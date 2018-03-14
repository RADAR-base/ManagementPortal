package org.radarcns.management.domain.support;

import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.radarcns.management.domain.AbstractEntity;
import org.radarcns.management.domain.audit.CustomRevisionEntity;
import org.radarcns.management.security.SpringSecurityAuditorAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.PostLoad;
import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Entity listener that contains all listeners that should fire on AbstractEntity's. This
 * listener will publish entity events to log, to have a uniform, centralized logging of entity
 * operations. Also, it will populate the created_by, created_at, last_modified_by and
 * last_modified_at fields using Envers audits when an entity is loaded.
 */
@Component
public class AbstractEntityListener {

    public static final String ENTITY_CREATED = "ENTITY_CREATED";
    public static final String ENTITY_UPDATED = "ENTITY_UPDATED";
    public static final String ENTITY_REMOVED = "ENTITY_REMOVED";

    private final Logger logger = LoggerFactory.getLogger(AbstractEntityListener.class);
    private static final String TEMPLATE = "[{}] by {}: entityClass={}, entity={}";

    @Autowired
    private EntityManager em;

    @Autowired
    private SpringSecurityAuditorAware springSecurityAuditorAware;

    /**
     * Event listener to log a persist event.
     *
     * @param entity the entity that is persisted
     */
    @PostPersist
    public void publishPersistEvent(AbstractEntity entity) {
        AutowireHelper.autowire(this, springSecurityAuditorAware);
        logger.info(TEMPLATE, ENTITY_CREATED, springSecurityAuditorAware.getCurrentAuditor(),
                entity.getClass().getName(), entity.toString());
    }

    /**
     * Event listener to log an update event.
     *
     * @param entity the entity that is updated
     */
    @PostUpdate
    public void publishUpdateEvent(AbstractEntity entity) {
        AutowireHelper.autowire(this, springSecurityAuditorAware);
        logger.info(TEMPLATE, ENTITY_UPDATED, springSecurityAuditorAware.getCurrentAuditor(),
                entity.getClass().getName(), entity.toString());
    }

    /**
     * Event listener to log a remove event.
     *
     * @param entity the entity that is removed
     */
    @PostRemove
    public void publishRemoveEvent(AbstractEntity entity) {
        AutowireHelper.autowire(this, springSecurityAuditorAware);
        logger.info(TEMPLATE, ENTITY_REMOVED, springSecurityAuditorAware.getCurrentAuditor(),
                entity.getClass().getName(), entity.toString());
    }

    /**
     * Event listener to populate audit metadata.
     *
     * @param entity the entity that was loaded
     */
    @PostLoad
    public void populateAuditMetaData(AbstractEntity entity) {
        AutowireHelper.autowire(this, em);
        AuditReader auditReader = AuditReaderFactory.get(em);

        List<Number> revisions = auditReader.getRevisions(entity.getClass(), entity.getId());
        Number first = Collections.min(revisions, Comparator.comparingLong(Number::longValue));
        Number last = Collections.max(revisions, Comparator.comparingLong(Number::longValue));

        CustomRevisionEntity firstRevision = auditReader.findRevision(CustomRevisionEntity.class,
                first);
        CustomRevisionEntity lastRevision = auditReader.findRevision(CustomRevisionEntity.class,
                last);

        entity.setCreatedDate(ZonedDateTime.ofInstant(firstRevision.getTimestamp().toInstant(),
                ZoneOffset.UTC));
        entity.setCreatedBy(firstRevision.getAuditor());
        entity.setLastModifiedDate(ZonedDateTime.ofInstant(lastRevision.getTimestamp().toInstant(),
                ZoneOffset.UTC));
        entity.setLastModifiedBy(lastRevision.getAuditor());
        logger.info("Populated audit data for entity {}", entity.toString());
    }
}
