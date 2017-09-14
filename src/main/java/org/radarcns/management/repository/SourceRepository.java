package org.radarcns.management.repository;

import org.radarcns.management.domain.Source;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for the Source entity.
 */
@SuppressWarnings("unused")
public interface SourceRepository extends JpaRepository<Source,Long> {

    List<Source> findAllSourcesByAssigned(@Param("assigned") Boolean assigned);

    List<Source> findAllSourcesByProjectId(@Param("projectId") Long projectId);

    List<Source> findAllSourcesByProjectIdAndAssigned(@Param("projectId") Long projectId , @Param("assigned") Boolean assigned);

    Optional<Source> findOneBySourceId(String sourceId);

    Optional<Source> findOneBySourceName(String sourceName);
}
