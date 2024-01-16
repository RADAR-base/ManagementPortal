package org.radarbase.management.repository

import org.radarbase.management.domain.Role
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.RepositoryDefinition
import org.springframework.data.repository.history.RevisionRepository
import org.springframework.data.repository.query.Param

/**
 * Created by nivethika on 18-5-17.
 */
@Suppress("unused")
@RepositoryDefinition(domainClass = Role::class, idClass = Long::class)
interface RoleRepository : JpaRepository<Role, Long>, RevisionRepository<Role, Long, Int> {
    @Query(
        "select role from Role role inner join role.authority authority"
                + " where authority.name = :authorityName"
    )
    fun findRolesByAuthorityName(@Param("authorityName") authorityName: String?): List<Role>

    @Query("select distinct role from Role role left join fetch role.authority")
    fun findAllWithEagerRelationships(): List<Role>

    @Query(
        "select role from Role role join role.authority "
                + "where role.organization.id = :organizationId "
                + "and role.authority.name = :authorityName"
    )
    fun findOneByOrganizationIdAndAuthorityName(
        @Param("organizationId") organizationId: Long?,
        @Param("authorityName") authorityName: String?
    ): Role?

    @Query(
        "select role from Role role join role.authority "
                + "where role.project.id = :projectId "
                + "and role.authority.name = :authorityName"
    )
    fun findOneByProjectIdAndAuthorityName(
        @Param("projectId") projectId: Long?,
        @Param("authorityName") authorityName: String?
    ): Role?

    @Query(
        "select role from Role role join role.authority join role.project "
                + "where role.project.projectName = :projectName "
                + "and role.authority.name = :authorityName"
    )
    fun findOneByProjectNameAndAuthorityName(
        @Param("projectName") projectName: String?,
        @Param("authorityName") authorityName: String?
    ): Role?

    @Query(
        "select role from Role role left join fetch role.authority "
                + "where role.project.projectName = :projectName"
    )
    fun findAllRolesByProjectName(@Param("projectName") projectName: String): List<Role>
}
