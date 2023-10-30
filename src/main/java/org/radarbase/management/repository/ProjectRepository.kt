package org.radarbase.management.repository

import org.radarbase.management.domain.Project
import org.radarbase.management.domain.SourceType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.RepositoryDefinition
import org.springframework.data.repository.history.RevisionRepository
import org.springframework.data.repository.query.Param

/**
 * Spring Data JPA repository for the Project entity.
 */
@Suppress("unused")
@RepositoryDefinition(domainClass = Project::class, idClass = Long::class)
interface ProjectRepository : JpaRepository<Project, Long>, RevisionRepository<Project, Long, Int> {
    @Query(
        value = "select distinct project from Project project "
                + "left join fetch project.sourceTypes",
        countQuery = "select distinct count(project) from Project project"
    )
    fun findAllWithEagerRelationships(pageable: Pageable): Page<Project>

    @Query(
        value = "select distinct project from Project project "
                + "left join fetch project.sourceTypes "
                + "WHERE project.projectName in (:projectNames) "
                + "OR project.organization.name in (:organizationNames)",
        countQuery = "select distinct count(project) from Project project "
                + "WHERE project.projectName in (:projectNames) "
                + "OR project.organization.name in (:organizationNames)"
    )
    fun findAllWithEagerRelationshipsInOrganizationsOrProjects(
        pageable: Pageable?,
        @Param("organizationNames") organizationNames: Collection<String>,
        @Param("projectNames") projectNames: Collection<String>
    ): Page<Project>

    @Query(
        "select project from Project project "
                + "WHERE project.organization.name = :organization_name"
    )
    fun findAllByOrganizationName(
        @Param("organization_name") organizationName: String
    ): List<Project>

    @Query(
        "select project from Project project "
                + "left join fetch project.sourceTypes s "
                + "left join fetch project.groups "
                + "left join fetch project.organization "
                + "where project.id = :id"
    )
    fun findOneWithEagerRelationships(@Param("id") id: Long): Project?

    @Query(
        "select project from Project project "
                + "left join fetch project.organization "
                + "where project.id = :id"
    )
    fun findByIdWithOrganization(@Param("id") id: Long?): Project?

    @Query(
        "select project from Project project "
                + "left join fetch project.sourceTypes "
                + "left join fetch project.groups "
                + "left join fetch project.organization "
                + "where project.projectName = :name"
    )
    fun findOneWithEagerRelationshipsByName(@Param("name") name: String?): Project?

    @Query(
        "select project.id from Project project "
                + "where project.projectName =:name"
    )
    fun findProjectIdByName(@Param("name") name: String): Long?

    @Query(
        "select project from Project project "
                + "left join fetch project.groups "
                + "where project.projectName = :name"
    )
    fun findOneWithGroupsByName(@Param("name") name: String): Project?

    @Query("select project.sourceTypes from Project project WHERE project.id = :id")
    fun findSourceTypesByProjectId(@Param("id") id: Long): List<SourceType>

    @Query(
        "select distinct sourceType from Project project "
                + "left join project.sourceTypes sourceType "
                + "where project.id =:id "
                + "and sourceType.id = :sourceTypeId "
    )
    fun findSourceTypeByProjectIdAndSourceTypeId(
        @Param("id") id: Long?,
        @Param("sourceTypeId") sourceTypeId: Long?
    ): SourceType?
}
