package org.radarbase.management.repository;

import org.radarbase.management.domain.Project;
import org.radarbase.management.domain.SourceType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.RepositoryDefinition;
import org.springframework.data.repository.history.RevisionRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for the Project entity.
 */
@SuppressWarnings("unused")
@RepositoryDefinition(domainClass = Project.class, idClass = Long.class)
public interface ProjectRepository extends JpaRepository<Project, Long>,
        RevisionRepository<Project, Long, Integer> {
    @Query(value = "select distinct project from Project project "
            + "left join fetch project.sourceTypes",
            countQuery = "select distinct count(project) from Project project")
    Page<Project> findAllWithEagerRelationships(Pageable pageable);

    @Query("select project from Project project "
            + "left join fetch project.sourceTypes s "
            + "left join fetch project.groups "
            + "where project.id = :id")
    Optional<Project> findOneWithEagerRelationships(@Param("id") Long id);

    @Query("select project from Project project "
            + "left join fetch project.sourceTypes "
            + "left join fetch project.groups "
            + "where project.projectName = :name")
    Optional<Project> findOneWithEagerRelationshipsByName(@Param("name") String name);

    @Query("select project.id from Project project "
            + "where project.projectName =:name")
    Optional<Long> findProjectIdByName(@Param("name") String name);

    @Query("select project from Project project "
            + "left join fetch project.groups "
            + "where project.projectName = :name")
    Optional<Project> findOneWithGroupsByName(@Param("name") String name);

    @Query("select project.sourceTypes from Project project WHERE project.id = :id")
    List<SourceType> findSourceTypesByProjectId(@Param("id") Long id);

    @Query("select distinct sourceType from Project project "
            + "left join project.sourceTypes sourceType "
            + "where project.id =:id "
            + "and sourceType.id = :sourceTypeId ")
    Optional<SourceType> findSourceTypeByProjectIdAndSourceTypeId(@Param("id") Long id,
            @Param("sourceTypeId") Long sourceTypeId);
}
