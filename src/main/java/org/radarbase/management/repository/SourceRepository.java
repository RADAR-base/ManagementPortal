package org.radarbase.management.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.hibernate.annotations.Where;
import org.radarbase.management.domain.Source;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.RepositoryDefinition;
import org.springframework.data.repository.history.RevisionRepository;
import org.springframework.data.repository.query.Param;

/**
 * Spring Data JPA repository for the Source entity.
 */
@RepositoryDefinition(domainClass = Source.class, idClass = Long.class)
public interface SourceRepository extends JpaRepository<Source, Long>,
        RevisionRepository<Source, Long, Integer> {
    @Where(clause = "deleted = false")
    Page<Source> findAllSourcesByProjectId(Pageable pageable, @Param("projectId") Long projectId);

    @Where(clause = "deleted = false")
    List<Source> findAllSourcesByProjectIdAndAssigned(@Param("projectId") Long projectId,
            @Param("assigned") Boolean assigned);

    @Where(clause = "deleted = false")
    Optional<Source> findOneBySourceId(UUID sourceId);

    @Where(clause = "deleted = false")
    Optional<Source> findOneBySourceName(String sourceName);
}
