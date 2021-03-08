package org.radarbase.management.repository;

import java.util.List;
import java.util.Optional;
import org.radarbase.management.domain.Project;
import org.radarbase.management.domain.SourceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.RepositoryDefinition;
import org.springframework.data.repository.history.RevisionRepository;
import org.springframework.data.repository.query.Param;

/**
 * Spring Data JPA repository for the SourceType entity.
 */
@SuppressWarnings("unused")
@RepositoryDefinition(domainClass = SourceType.class, idClass = Long.class)
public interface SourceTypeRepository extends JpaRepository<SourceType, Long>,
        RevisionRepository<SourceType, Long, Integer> {

    @Query("select distinct sourceType from SourceType sourceType left join fetch sourceType"
            + ".sourceData")
    List<SourceType> findAllWithEagerRelationships();

    @Query("select sourceType from SourceType sourceType left join fetch sourceType.sourceData "
            + "where sourceType.producer =:producer "
            + "and sourceType.model =:model "
            + "and sourceType.catalogVersion = :version")
    Optional<SourceType> findOneWithEagerRelationshipsByProducerAndModelAndVersion(
            @Param("producer") String producer, @Param("model") String model,
            @Param("version") String version);

    @Query("select sourceType from SourceType sourceType left join fetch sourceType.sourceData "
            + "where sourceType.producer =:producer")
    List<SourceType> findWithEagerRelationshipsByProducer(@Param("producer") String producer);

    @Query("select sourceType from SourceType sourceType left join fetch sourceType.sourceData "
            + "where sourceType.producer =:producer and sourceType.model =:model")
    List<SourceType> findWithEagerRelationshipsByProducerAndModel(
            @Param("producer") String producer, @Param("model") String model);

    @Query("select distinct sourceType.projects from SourceType sourceType left join sourceType"
            + ".projects where sourceType.producer =:producer and sourceType.model =:model "
            + "and sourceType.catalogVersion = :version")
    List<Project> findProjectsBySourceType(@Param("producer") String producer,
            @Param("model") String model, @Param("version") String version);
}
