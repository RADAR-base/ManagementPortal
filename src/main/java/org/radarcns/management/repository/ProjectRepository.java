package org.radarcns.management.repository;

import java.util.List;
import java.util.Optional;
import org.radarcns.management.domain.Project;
import org.radarcns.management.domain.SourceType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Spring Data JPA repository for the Project entity.
 */
@SuppressWarnings("unused")
public interface ProjectRepository extends JpaRepository<Project, Long> {

    @Query(value = "select distinct project from Project project "
            + "left join fetch project.sourceTypes",
            countQuery = "select distinct count(project) from Project project")
    Page<Project> findAllWithEagerRelationships(Pageable pageable);

    @Query("select project from Project project left join fetch "
            + "project.sourceTypes where project.id =:id")
    Project findOneWithEagerRelationships(@Param("id") Long id);

    @Query("select project from Project project left join fetch "
            + "project.sourceTypes where project.projectName =:name")
    Project findOneWithEagerRelationshipsByName(@Param("name") String name);

    @Query("select project.sourceTypes from Project project WHERE project.id = :id")
    List<SourceType> findSourceTypesByProjectId(@Param("id") Long id);

    @Query(
            "select distinct sourceType from Project project left join project.sourceTypes sourceType "
            + "where project.id =:id "
            + "and sourceType.id = :sourceTypeId ")
    Optional<SourceType> findSourceTypeByProjectIdAndSourceTypeId(@Param("id") Long id,
        @Param("sourceTypeId") Long sourceTypeId);
}
