package org.radarcns.management.domain.support;

import org.radarcns.management.domain.AbstractAuditingEntity;
import org.radarcns.management.security.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;

/**
 * Entity listener that publishes entity events to log, to have a uniform, centralized logging of
 * entity operations.
 */
@Component
public class LogPublisherEntityListener {

    private final Logger logger = LoggerFactory.getLogger(LogPublisherEntityListener.class);
    private static final String TEMPLATE = "[{}] by {}: entityClass={}, entity={}";

    @PostPersist
    public void publishPersistEvent(AbstractAuditingEntity entity) {
        logger.info(TEMPLATE, EventPublisherEntityListener.ENTITY_CREATED,
                entity.getCreatedBy(), entity.getClass().getName(), entity.toString());
    }

    @PostUpdate
    public void publishUpdateEvent(AbstractAuditingEntity entity) {
        logger.info(TEMPLATE, EventPublisherEntityListener.ENTITY_UPDATED,
                entity.getLastModifiedBy(), entity.getClass().getName(), entity.toString());
    }

    @PostRemove
    public void publishRemoveEvent(AbstractAuditingEntity entity) {
        logger.info(TEMPLATE, EventPublisherEntityListener.ENTITY_REMOVED,
                SecurityUtils.getCurrentUserLogin(), entity.getClass().getName(), entity.toString());
    }
}
