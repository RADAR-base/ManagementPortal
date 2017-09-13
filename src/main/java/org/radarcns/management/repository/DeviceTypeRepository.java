package org.radarcns.management.repository;

import org.radarcns.management.domain.DeviceType;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Spring Data JPA repository for the DeviceType entity.
 */
@SuppressWarnings("unused")
public interface DeviceTypeRepository extends JpaRepository<DeviceType,Long> {

    @Query("select distinct deviceType from DeviceType deviceType left join fetch deviceType.sensorData")
    List<DeviceType> findAllWithEagerRelationships();

    @Query("select deviceType from DeviceType deviceType left join fetch deviceType.sensorData where deviceType.id =:id")
    DeviceType findOneWithEagerRelationships(@Param("id") Long id);

    @Query("select deviceType from DeviceType deviceType left join fetch deviceType.sensorData "
        + "where deviceType.deviceProducer =:producer and deviceType.deviceModel =:model")
    DeviceType findOneWithEagerRelationshipsByProducerAndModel(
        @Param("producer") String producer, @Param("model") String model);

    @Query("select deviceType from DeviceType deviceType left join fetch deviceType.sensorData "
        + "where deviceType.deviceProducer =:producer")
    List<DeviceType> findWithEagerRelationshipsByProducer(@Param("producer") String producer);

    @Query("select deviceType from DeviceType deviceType left join fetch deviceType.sensorData "
        + "where deviceType.deviceModel =:model")
    List<DeviceType> findWithEagerRelationshipsByModel(@Param("model") String model);
}
