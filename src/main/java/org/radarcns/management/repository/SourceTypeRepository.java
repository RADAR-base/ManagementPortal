package org.radarcns.management.repository;

import java.util.Optional;
import org.radarcns.management.domain.SourceType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Spring Data JPA repository for the SourceType entity.
 */
@SuppressWarnings("unused")
public interface SourceTypeRepository extends JpaRepository<SourceType,Long> {

    @Query("select distinct sourceType from SourceType sourceType left join fetch sourceType.sourceData")
    List<SourceType> findAllWithEagerRelationships();

    @Query("select sourceType from SourceType sourceType left join fetch sourceType.sourceData where sourceType.id =:id")
    SourceType findOneWithEagerRelationships(@Param("id") Long id);

    @Query("select sourceType from SourceType sourceType left join fetch sourceType.sourceData "
        + "where sourceType.deviceProducer =:producer "
        + "and sourceType.deviceModel =:model "
        + "and sourceType.catalogVersion = :version")
    Optional<SourceType> findOneWithEagerRelationshipsByProducerAndModelAndVersion(
        @Param("producer") String producer, @Param("model") String model , @Param("version") String version);

    @Query("select sourceType from SourceType sourceType left join fetch sourceType.sourceData "
        + "where sourceType.deviceProducer =:producer")
    List<SourceType> findWithEagerRelationshipsByProducer(@Param("producer") String producer);

    @Query("select sourceType from SourceType sourceType left join fetch sourceType.sourceData "
        + "where sourceType.deviceProducer =:producer and sourceType.deviceModel =:model")
    List<SourceType> findWithEagerRelationshipsByProducerAndModel(
        @Param("producer") String producer, @Param("model") String model);
}
