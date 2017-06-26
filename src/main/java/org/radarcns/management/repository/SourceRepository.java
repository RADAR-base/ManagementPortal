package org.radarcns.management.repository;

import java.util.Optional;
import org.radarcns.management.domain.Source;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

/**
 * Spring Data JPA repository for the Source entity.
 */
@SuppressWarnings("unused")
public interface SourceRepository extends JpaRepository<Source,Long> {

    List<Source> findAllSourcesByAssigned(@Param("assigned") Boolean assigned);

    List<Source> findAllSourcesByProjectId(@Param("projectId") Long projectId);

    List<Source> findAllSourcesByProjectIdAndAssigned(@Param("projectId") Long projectId , @Param("assigned") Boolean assigned);

    Optional<Source> findOneBySourceId(String sourceId);
}
