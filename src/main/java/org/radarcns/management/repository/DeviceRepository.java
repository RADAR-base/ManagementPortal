package org.radarcns.management.repository;

import org.radarcns.management.domain.Device;

import org.springframework.data.jpa.repository.*;

import java.util.List;
import org.springframework.data.repository.query.Param;

/**
 * Spring Data JPA repository for the Device entity.
 */
@SuppressWarnings("unused")
public interface DeviceRepository extends JpaRepository<Device,Long> {

    List<Device> findAllDevicesByAssigned(@Param("assigned") Boolean assigned);

}
