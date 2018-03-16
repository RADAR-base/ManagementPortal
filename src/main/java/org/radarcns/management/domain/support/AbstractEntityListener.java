package org.radarcns.management.domain.support;

import org.radarcns.management.domain.AbstractEntity;
import org.radarcns.management.domain.audit.CustomRevisionEntity;
import org.radarcns.management.security.SpringSecurityAuditorAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.history.Revision;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.RepositoryDefinition;
import org.springframework.data.repository.core.support.DefaultRepositoryMetadata;
import org.springframework.data.repository.history.RevisionRepository;
import org.springframework.stereotype.Component;

import javax.persistence.PostLoad;
import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

    @Autowired
    private List<Repository> repositories;

    private final Map<Class, Optional<RevisionRepository>> repositoryMap = new HashMap<>();

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
     */
    @PostLoad
    public void populateAuditMetaData(AbstractEntity entity) {
        if (entity == null) {
            return;
        }
        log.debug("Populating audit metadata for class {}", entity.getClass().getName());
        Optional<RevisionRepository> repo = getRepository(entity);
        if (!repo.isPresent()) {
            log.debug("No RevisionRepository found for class {}, not populating any metadata",
                    entity.getClass().getName());
            return;
        }
        // Now get the list of revisions for the entity
        List<Revision> revisions = repo.get().findRevisions(entity.getId()).getContent();
        // Revision implements Comparable
        Object first = Collections.min(revisions).getMetadata().getDelegate();
        Object last = Collections.max(revisions).getMetadata().getDelegate();
        if (first instanceof CustomRevisionEntity) {
            CustomRevisionEntity revisionEntity = (CustomRevisionEntity) first;
            entity.setCreatedBy(revisionEntity.getAuditor());
            entity.setCreatedDate(ZonedDateTime.ofInstant(revisionEntity.getTimestamp().toInstant(),
                    ZoneOffset.UTC));
        } else {
            log.warn("First revision info for class {} was not of type {}, but was of type {}",
                    entity.getClass().getName(), CustomRevisionEntity.class.getName(),
                    first.getClass().getName());
        }
        if (last instanceof CustomRevisionEntity) {
            CustomRevisionEntity revisionEntity = (CustomRevisionEntity) last;
            entity.setLastModifiedBy(revisionEntity.getAuditor());
            entity.setLastModifiedDate(ZonedDateTime.ofInstant(
                    revisionEntity.getTimestamp().toInstant(), ZoneOffset.UTC));
        } else {
            log.warn("First revision info for class {} was not of type {}, but was of type {}",
                    entity.getClass().getName(), CustomRevisionEntity.class.getName(),
                    last.getClass().getName());
        }
    }

    /**
     * Find the RevisionRepository for a given entity. This will cache the result in the local
     * repositoryMap field, and return future requests for the same entity from that map.
     *
     * @param entity the entity to find a repository for
     * @return an {@link Optional} that contains the repository if it was found.
     */
    private Optional<RevisionRepository> getRepository(AbstractEntity entity) {
        if (repositoryMap.containsKey(entity.getClass())) {
            return repositoryMap.get(entity.getClass());
        }
        AutowireHelper.autowire(this, repositories);
        // Find a repository that is a RevisionRepository, has the RepositoryDefinition
        // annotation which is needed for DefaultRepositoryMetadata, and has the correct domain type
        Optional<RevisionRepository> result = repositories.stream()
                .filter(repo -> repo instanceof RevisionRepository)
                .filter(repo -> Arrays.stream(repo.getClass().getInterfaces())
                        .anyMatch(repoInterface -> repoInterface
                                .isAnnotationPresent(RepositoryDefinition.class)
                                && DefaultRepositoryMetadata.getMetadata(repoInterface)
                                        .getDomainType().equals(entity.getClass()))
        ).findFirst().map(repo -> (RevisionRepository) repo);
        repositoryMap.put(entity.getClass(), result);
        return result;
    }
}
