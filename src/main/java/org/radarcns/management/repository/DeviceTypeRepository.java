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

}
