package org.radarbase.management.repository

import org.radarbase.management.domain.Organization
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.RepositoryDefinition
import org.springframework.data.repository.history.RevisionRepository
import org.springframework.data.repository.query.Param

/**
 * Spring Data JPA repository for the Organization entity.
 */
@RepositoryDefinition(domainClass = Organization::class, idClass = Long::class)
interface OrganizationRepository :
    JpaRepository<Organization, Long>,
    RevisionRepository<Organization, Long, Int> {
    @Query(
        "select org from Organization org " +
            "where org.name = :name",
    )
    fun findOneByName(
        @Param("name") name: String,
    ): Organization?

    @Query(
        "select distinct org from Organization org left join fetch org.projects project " +
            "where project.projectName in (:projectNames)",
    )
    fun findAllByProjectNames(
        @Param("projectNames") projectNames: Collection<String>,
    ): List<Organization>
}
