package org.radarcns.management.repository;

import org.radarcns.management.domain.DeviceType;

import org.springframework.data.jpa.repository.*;

import java.util.List;

/**
 * Spring Data JPA repository for the DeviceType entity.
 */
@SuppressWarnings("unused")
public interface DeviceTypeRepository extends JpaRepository<DeviceType,Long> {

}
