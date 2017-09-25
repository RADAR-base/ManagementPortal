package org.radarcns.management.repository;

import java.util.Optional;
import org.radarcns.management.domain.DeviceType;
import org.radarcns.management.domain.Project;
import org.radarcns.management.domain.Source;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Spring Data JPA repository for the Project entity.
 */
@SuppressWarnings("unused")
public interface ProjectRepository extends JpaRepository<Project,Long> {

    @Query("select distinct project from Project project left join fetch project.deviceTypes")
    List<Project> findAllWithEagerRelationships();

    @Query("select project from Project project left join fetch project.deviceTypes where project.id =:id")
    Project findOneWithEagerRelationships(@Param("id") Long id);

    @Query("select project from Project project left join fetch project.deviceTypes where project.projectName =:name")
    Project findOneWithEagerRelationshipsByName(@Param("name") String name);

    @Query("select project.deviceTypes from Project project WHERE project.id = :id")
    List<DeviceType> findDeviceTypesByProjectId(@Param("id") Long id);

    @Query("select distinct deviceType from Project project left join project.deviceTypes deviceType "
        + "where project.id =:id "
        + "and deviceType.deviceProducer = :producer "
        + "and deviceType.deviceModel = :model "
        + "and deviceType.catalogVersion =:version")
    Optional<DeviceType> findDeviceTypeByProjectIdAndDeviceTypeProp(@Param("id") Long id,
        @Param("producer") String producer, @Param("model") String model,
        @Param("version") String version);

}
