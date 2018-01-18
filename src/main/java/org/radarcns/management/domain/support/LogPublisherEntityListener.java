package org.radarcns.management.domain.support;

import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;
import org.radarcns.management.domain.AbstractAuditingEntity;
import org.radarcns.management.security.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Entity listener that publishes entity events to log, to have a uniform, centralized logging of
 * entity operations.
 */
@Component
public class LogPublisherEntityListener {

    private final Logger logger = LoggerFactory.getLogger(LogPublisherEntityListener.class);
    private static final String TEMPLATE = "[{}] by {}: entityClass={}, entity={}";

    /**
     * Event listener to log a persist event.
     *
     * @param entity the entity that is persisted
     */
    @PostPersist
    public void publishPersistEvent(AbstractAuditingEntity entity) {
        logger.info(TEMPLATE, EventPublisherEntityListener.ENTITY_CREATED,
                entity.getCreatedBy(), entity.getClass().getName(), entity.toString());
    }

    /**
     * Event listener to log an update event.
     *
     * @param entity the entity that is updated
     */
    @PostUpdate
    public void publishUpdateEvent(AbstractAuditingEntity entity) {
        logger.info(TEMPLATE, EventPublisherEntityListener.ENTITY_UPDATED,
                entity.getLastModifiedBy(), entity.getClass().getName(), entity.toString());
    }

    /**
     * Event listener to log a remove event.
     *
     * @param entity the entity that is removed
     */
    @PostRemove
    public void publishRemoveEvent(AbstractAuditingEntity entity) {
        logger.info(TEMPLATE, EventPublisherEntityListener.ENTITY_REMOVED,
                SecurityUtils.getCurrentUserLogin(), entity.getClass().getName(),
                entity.toString());
    }
}
