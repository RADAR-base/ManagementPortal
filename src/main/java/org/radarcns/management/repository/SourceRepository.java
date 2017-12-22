package org.radarcns.management.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.radarcns.management.domain.Source;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

/**
 * Spring Data JPA repository for the Source entity.
 */
@SuppressWarnings("unused")
public interface SourceRepository extends JpaRepository<Source, Long> {

    List<Source> findAllSourcesByAssigned(@Param("assigned") Boolean assigned);

    Page<Source> findAllSourcesByProjectId(Pageable pageable, @Param("projectId") Long projectId);

    List<Source> findAllSourcesByProjectIdAndAssigned(@Param("projectId") Long projectId,
            @Param("assigned") Boolean assigned);

    Optional<Source> findOneBySourceId(UUID sourceId);

    Optional<Source> findOneBySourceName(String sourceName);
}
