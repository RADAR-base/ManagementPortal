package org.radarbase.management.domain.support

import org.radarbase.management.domain.AbstractEntity
import org.radarbase.management.security.Constants
import org.radarbase.management.security.SpringSecurityAuditorAware
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import javax.persistence.PostPersist
import javax.persistence.PostRemove
import javax.persistence.PostUpdate

/**
 * Entity listener that contains all listeners that should fire on AbstractEntity's. This
 * listener will publish entity events to log, to have a uniform, centralized logging of entity
 * operations. Also, it will populate the created_by, created_at, last_modified_by and
 * last_modified_at fields using Envers audits when an entity is loaded.
 */
@Component
class AbstractEntityListener {
    @Autowired
    private val springSecurityAuditorAware: SpringSecurityAuditorAware? = null

    /**
     * Event listener to log a persist event.
     *
     * @param entity the entity that is persisted
     */
    @PostPersist
    fun publishPersistEvent(entity: AbstractEntity) {
        AutowireHelper.autowire(this, springSecurityAuditorAware)
        log.info(
            TEMPLATE, ENTITY_CREATED, springSecurityAuditorAware!!.currentAuditor
                .orElse(Constants.SYSTEM_ACCOUNT),
            entity.javaClass.getName(), entity.toString()
        )
    }

    /**
     * Event listener to log an update event.
     *
     * @param entity the entity that is updated
     */
    @PostUpdate
    fun publishUpdateEvent(entity: AbstractEntity) {
        AutowireHelper.autowire(this, springSecurityAuditorAware)
        log.info(
            TEMPLATE, ENTITY_UPDATED, springSecurityAuditorAware!!.currentAuditor
                .orElse(Constants.SYSTEM_ACCOUNT),
            entity.javaClass.getName(), entity.toString()
        )
    }

    /**
     * Event listener to log a remove event.
     *
     * @param entity the entity that is removed
     */
    @PostRemove
    fun publishRemoveEvent(entity: AbstractEntity) {
        AutowireHelper.autowire(this, springSecurityAuditorAware)
        log.info(
            TEMPLATE, ENTITY_REMOVED, springSecurityAuditorAware!!.currentAuditor
                .orElse(Constants.SYSTEM_ACCOUNT),
            entity.javaClass.getName(), entity.toString()
        )
    }

    /**
     * When an entity is loaded, find out the repository of the entity, load the revision log,
     * and use it to populate the created and last modified fields.
     *
     * @param entity the entity that was loaded.
     */
    /*
    @PostLoad
    public void populateAuditMetaData(AbstractEntity entity) {

    }*/
    companion object {
        const val ENTITY_CREATED = "ENTITY_CREATED"
        const val ENTITY_UPDATED = "ENTITY_UPDATED"
        const val ENTITY_REMOVED = "ENTITY_REMOVED"
        private val log = LoggerFactory.getLogger(AbstractEntityListener::class.java)
        private const val TEMPLATE = "[{}] by {}: entityClass={}, entity={}"
    }
}
