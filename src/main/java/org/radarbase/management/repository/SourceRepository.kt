package org.radarbase.management.repository

import org.radarbase.management.domain.Source
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.RepositoryDefinition
import org.springframework.data.repository.history.RevisionRepository
import org.springframework.data.repository.query.Param
import java.util.*

/**
 * Spring Data JPA repository for the Source entity.
 */
@RepositoryDefinition(domainClass = Source::class, idClass = Long::class)
interface SourceRepository : JpaRepository<Source, Long>, RevisionRepository<Source, Long, Int> {
    @Query(
        value = "select source from Source source "
                + "WHERE source.deleted = false", countQuery = "select count(source) from Source source "
                + "WHERE source.deleted = false"
    )
    override fun findAll(pageable: Pageable): Page<Source>

    @Query(
        value = "select source from Source source "
                + "WHERE source.deleted = false "
                + "AND source.project.id = :projectId", countQuery = "select count(source) from Source source "
                + "WHERE source.deleted = false "
                + "AND source.project.id = :projectId"
    )
    fun findAllSourcesByProjectId(pageable: Pageable, @Param("projectId") projectId: Long): Page<Source>

    @Query(
        value = "select source from Source source "
                + "WHERE source.deleted = false "
                + "AND source.project.id = :projectId "
                + "AND source.assigned = :assigned", countQuery = "select count(source) from Source source "
                + "WHERE source.deleted = false "
                + "AND source.project.id = :projectId "
                + "AND source.assigned = :assigned"
    )
    fun findAllSourcesByProjectIdAndAssigned(
        @Param("projectId") projectId: Long?,
        @Param("assigned") assigned: Boolean?
    ): List<Source>

    @Query(
        value = "select source from Source source "
                + "WHERE source.deleted = false "
                + "AND source.sourceId = :sourceId", countQuery = "select count(source) from Source source "
                + "WHERE source.deleted = false "
                + "AND source.sourceId = :sourceId"
    )
    fun findOneBySourceId(@Param("sourceId") sourceId: UUID?): Source?

    @Query(
        value = "select source from Source source "
                + "WHERE source.deleted = false "
                + "AND source.sourceName = :sourceName", countQuery = "select count(source) from Source source "
                + "WHERE source.deleted = false "
                + "AND source.sourceName = :sourceName"
    )
    fun findOneBySourceName(@Param("sourceName") sourceName: String): Source?
}
