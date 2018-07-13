package org.radarcns.management.domain.support;

import org.radarcns.management.domain.AbstractEntity;
import org.radarcns.management.security.SpringSecurityAuditorAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;

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

    private final Logger log = LoggerFactory.getLogger(AbstractEntityListener.class);
    private static final String TEMPLATE = "[{}] by {}: entityClass={}, entity={}";

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
        log.info(TEMPLATE, ENTITY_CREATED, springSecurityAuditorAware.getCurrentAuditor(),
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
        log.info(TEMPLATE, ENTITY_UPDATED, springSecurityAuditorAware.getCurrentAuditor(),
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
        log.info(TEMPLATE, ENTITY_REMOVED, springSecurityAuditorAware.getCurrentAuditor(),
                entity.getClass().getName(), entity.toString());
    }

    /**
     * When an entity is loaded, find out the repository of the entity, load the revision log,
     * and use it to populate the created and last modified fields.
     *
     * @param entity the entity that was loaded.
     *//*
    @PostLoad
    public void populateAuditMetaData(AbstractEntity entity) {

    }*/


}
