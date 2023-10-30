package org.radarbase.management.repository

import org.radarbase.management.domain.Project
import org.radarbase.management.domain.SourceType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.RepositoryDefinition
import org.springframework.data.repository.history.RevisionRepository
import org.springframework.data.repository.query.Param

/**
 * Spring Data JPA repository for the SourceType entity.
 */
@Suppress("unused")
@RepositoryDefinition(domainClass = SourceType::class, idClass = Long::class)
interface SourceTypeRepository : JpaRepository<SourceType, Long>, RevisionRepository<SourceType, Long, Int> {
    @Query(
        "select distinct sourceType from SourceType sourceType left join fetch sourceType"
                + ".sourceData"
    )
    fun findAllWithEagerRelationships(): List<SourceType>

    @Query(
        "select case when count(sourceType) > 0 then true else false end "
                + "from SourceType sourceType "
                + "where sourceType.producer = :producer "
                + "and sourceType.model = :model "
                + "and sourceType.catalogVersion = :version"
    )
    fun hasOneByProducerAndModelAndVersion(
        @Param("producer") producer: String, @Param("model") model: String,
        @Param("version") version: String
    ): Boolean

    @Query(
        "select sourceType from SourceType sourceType left join fetch sourceType.sourceData "
                + "where sourceType.producer = :producer "
                + "and sourceType.model = :model "
                + "and sourceType.catalogVersion = :version"
    )
    fun findOneWithEagerRelationshipsByProducerAndModelAndVersion(
        @Param("producer") producer: String, @Param("model") model: String,
        @Param("version") version: String
    ): SourceType?

    @Query(
        "select sourceType from SourceType sourceType left join fetch sourceType.sourceData "
                + "where sourceType.producer =:producer"
    )
    fun findWithEagerRelationshipsByProducer(@Param("producer") producer: String): List<SourceType>

    @Query(
        "select sourceType from SourceType sourceType left join fetch sourceType.sourceData "
                + "where sourceType.producer =:producer and sourceType.model =:model"
    )
    fun findWithEagerRelationshipsByProducerAndModel(
        @Param("producer") producer: String, @Param("model") model: String
    ): List<SourceType>

    @Query(
        "select distinct sourceType.projects from SourceType sourceType left join sourceType"
                + ".projects where sourceType.producer =:producer and sourceType.model =:model "
                + "and sourceType.catalogVersion = :version"
    )
    fun findProjectsBySourceType(
        @Param("producer") producer: String,
        @Param("model") model: String, @Param("version") version: String
    ): List<Project>
}
