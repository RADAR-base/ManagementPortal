package org.radarbase.management.repository;

import java.util.Optional;
import org.radarbase.management.domain.SourceData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.RepositoryDefinition;
import org.springframework.data.repository.history.RevisionRepository;

/**
 * Spring Data JPA repository for the SourceData entity.
 */
@SuppressWarnings("unused")
@RepositoryDefinition(domainClass = SourceData.class, idClass = Long.class)
public interface SourceDataRepository extends JpaRepository<SourceData, Long>,
        RevisionRepository<SourceData, Long, Integer> {

    Optional<SourceData> findOneBySourceDataName(String sourceDataName);
}
