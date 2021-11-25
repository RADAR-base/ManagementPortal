package org.radarbase.management.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.hibernate.annotations.Where;
import org.radarbase.management.domain.Source;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.RepositoryDefinition;
import org.springframework.data.repository.history.RevisionRepository;
import org.springframework.data.repository.query.Param;

/**
 * Spring Data JPA repository for the Source entity.
 */
@RepositoryDefinition(domainClass = Source.class, idClass = Long.class)
public interface SourceRepository extends JpaRepository<Source, Long>,
        RevisionRepository<Source, Long, Integer> {

    @Query(value = "select source from Source source "
            + "WHERE source.deleted = false",
            countQuery = "select count(source) from Source source "
            + "WHERE source.deleted = false")
    Page<Source> findAll(Pageable pageable);

    @Query(value = "select source from Source source "
            + "WHERE source.deleted = false "
            + "AND source.project.id = :projectId",
            countQuery = "select count(source) from Source source "
            + "WHERE source.deleted = false "
            + "AND source.project.id = :projectId")
    Page<Source> findAllSourcesByProjectId(Pageable pageable, @Param("projectId") Long projectId);

    @Query(value = "select source from Source source "
            + "WHERE source.deleted = false "
            + "AND source.project.id = :projectId "
            + "AND source.assigned = :assigned",
            countQuery = "select count(source) from Source source "
            + "WHERE source.deleted = false "
            + "AND source.project.id = :projectId "
            + "AND source.assigned = :assigned")
    List<Source> findAllSourcesByProjectIdAndAssigned(@Param("projectId") Long projectId,
            @Param("assigned") Boolean assigned);

    @Query(value = "select source from Source source "
            + "WHERE source.deleted = false "
            + "AND source.sourceId = :sourceId",
            countQuery = "select count(source) from Source source "
            + "WHERE source.deleted = false "
            + "AND source.sourceId = :sourceId")
    Optional<Source> findOneBySourceId(@Param("sourceId") UUID sourceId);

    @Query(value = "select source from Source source "
            + "WHERE source.deleted = false "
            + "AND source.sourceName = :sourceName",
            countQuery = "select count(source) from Source source "
            + "WHERE source.deleted = false "
            + "AND source.sourceName = :sourceName")
    Optional<Source> findOneBySourceName(@Param("sourceName") String sourceName);
}
