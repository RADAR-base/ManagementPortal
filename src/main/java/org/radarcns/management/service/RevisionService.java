package org.radarcns.management.service;

import org.radarcns.management.domain.AbstractEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.history.Revision;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.RepositoryDefinition;
import org.springframework.data.repository.core.support.DefaultRepositoryMetadata;
import org.springframework.data.repository.history.RevisionRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class RevisionService {

    @Autowired
    private List<Repository> repositories;

    private final Map<Class, Optional<RevisionRepository>> repositoryMap = new HashMap<>();

    private final Logger log = LoggerFactory.getLogger(RevisionService.class);

    /**
     * Search the audit log for when the given entity was created.
     *
     * @param entity the entity for which to find the creation time
     * @return an {@link Optional} of the {@link Instant} of the created time
     */
    public Optional<Instant> getCreatedAt(AbstractEntity entity) {
        Optional<RevisionRepository> repo = getRepository(entity);
        if (!repo.isPresent()) {
            log.debug("No RevisionRepository found for class {}", entity.getClass().getName());
            return Optional.empty();
        }
        try {
            List<Revision> revisions = repo.get().findRevisions(entity.getId()).getContent();
            return Optional.of(revisions.get(0).getRevisionDate().toDate().toInstant());
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return Optional.empty();
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
