package org.radarcns.management.repository;

import java.util.Optional;
import org.radarcns.management.domain.SourceType;
import org.radarcns.management.domain.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Spring Data JPA repository for the Project entity.
 */
@SuppressWarnings("unused")
public interface ProjectRepository extends JpaRepository<Project,Long> {

    @Query("select distinct project from Project project left join fetch project.sourceTypes")
    List<Project> findAllWithEagerRelationships();

    @Query("select project from Project project left join fetch project.sourceTypes where project.id =:id")
    Project findOneWithEagerRelationships(@Param("id") Long id);

    @Query("select project from Project project left join fetch project.sourceTypes where project.projectName =:name")
    Project findOneWithEagerRelationshipsByName(@Param("name") String name);

    @Query("select project.sourceTypes from Project project WHERE project.id = :id")
    List<SourceType> findSourceTypesByProjectId(@Param("id") Long id);

    @Query("select distinct sourceType from Project project left join project.sourceTypes sourceType "
        + "where project.id =:id "
        + "and sourceType.id = :sourceTypeId ")
    Optional<SourceType> findSourceTypeByProjectIdAndSourceTypeId(@Param("id") Long id,
        @Param("sourceTypeId") Long sourceTypeId);
}
