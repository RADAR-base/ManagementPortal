package org.radarbase.management.service

import org.radarbase.auth.authorization.RoleAuthority
import org.radarbase.auth.authorization.RoleAuthority.Companion.valueOfAuthority
import org.radarbase.management.domain.Authority
import org.radarbase.management.domain.Role
import org.radarbase.management.repository.AuthorityRepository
import org.radarbase.management.repository.OrganizationRepository
import org.radarbase.management.repository.ProjectRepository
import org.radarbase.management.repository.RoleRepository
import org.radarbase.management.service.dto.RoleDTO
import org.radarbase.management.service.mapper.RoleMapper
import org.radarbase.management.web.rest.errors.BadRequestException
import org.radarbase.management.web.rest.errors.EntityName
import org.radarbase.management.web.rest.errors.ErrorConstants
import org.radarbase.management.web.rest.errors.NotFoundException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*
import java.util.function.Consumer

/**
 * Service Implementation for managing Project.
 */
@Service
@Transactional
class RoleService(
    @Autowired private val roleRepository: RoleRepository,
    @Autowired private val authorityRepository: AuthorityRepository,
    @Autowired private val organizationRepository: OrganizationRepository,
    @Autowired private val projectRepository: ProjectRepository,
    @Autowired private val roleMapper: RoleMapper
) {
    @Autowired lateinit private var userService: UserService

    /**
     * Save a role.
     *
     * @param roleDto the entity to save
     * @return the persisted entity
     */
    fun save(roleDto: RoleDTO): RoleDTO? {
        log.debug("Request to save Role : {}", roleDto)
        var role = roleMapper.roleDTOToRole(roleDto)
        role = role?.let { roleRepository.save(it) }
        return role?.let { roleMapper.roleToRoleDTO(it) }
    }

    /**
     * Get the roles the currently authenticated user has access to.
     *
     *
     * A system administrator has access to all the roles. A project administrator has access
     * to the roles in their own project.
     *
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    fun findAll(): List<RoleDTO> {
        val optUser = userService.userWithAuthorities
            ?: // return an empty list if we do not have a current user (e.g. with client credentials
            // oauth2 grant)
            return emptyList()
        val currentUserAuthorities = optUser.authorities
        return if (currentUserAuthorities?.contains(RoleAuthority.SYS_ADMIN.authority) == true) {
            log.debug("Request to get all Roles")
            roleRepository.findAll().filterNotNull().map { role: Role -> roleMapper.roleToRoleDTO(role) }.toList()
        } else (if (currentUserAuthorities?.contains(RoleAuthority.PROJECT_ADMIN.authority) == true) {
            log.debug("Request to get project admin's project Projects")
            optUser.roles?.asSequence()?.filter { role: Role? ->
                (RoleAuthority.PROJECT_ADMIN.authority == role?.authority?.name)
            }?.mapNotNull { r: Role -> r.project?.projectName }?.distinct()
                ?.flatMap { name: String -> roleRepository.findAllRolesByProjectName(name) }
                ?.map { role -> roleMapper.roleToRoleDTO(role) }?.toList()
        } else {
            emptyList()
        }) as List<RoleDTO>
    }

    /**
     * Get all Admin roles.
     *
     * @return the list of entities
     */
    @Transactional(readOnly = true)
    fun findSuperAdminRoles(): List<RoleDTO> {
        log.debug("Request to get admin Roles")
        return roleRepository.findRolesByAuthorityName(RoleAuthority.SYS_ADMIN.authority)
            .map { role: Role -> roleMapper.roleToRoleDTO(role) }.toList()
    }

    /**
     * Get one role by id.
     *
     * @param id the id of the entity
     * @return the entity
     */
    @Transactional(readOnly = true)
    fun findOne(id: Long): RoleDTO {
        log.debug("Request to get Role : {}", id)
        val role = roleRepository.findById(id).get()
        return roleMapper.roleToRoleDTO(role)
    }

    /**
     * Delete the  role by id.
     *
     * @param id the id of the entity
     */
    fun delete(id: Long) {
        log.debug("Request to delete Role : {}", id)
        roleRepository.deleteById(id)
    }

    /**
     * Get or create given global role.
     * @param role to get or create
     * @return role from database
     */
    fun getGlobalRole(role: RoleAuthority): Role {
        return roleRepository.findRolesByAuthorityName(role.authority).firstOrNull()
            ?: createNewRole(role) { _: Role? -> }
    }


    /**
     * Get or create given organization role.
     * @param role to get or create
     * @param organizationId organization ID
     * @return role from database
     */
    fun getOrganizationRole(role: RoleAuthority, organizationId: Long): Role {
        return roleRepository.findOneByOrganizationIdAndAuthorityName(
            organizationId, role.authority
        )
            ?: createNewRole(role) { r: Role ->
                r.organization = organizationRepository.findById(organizationId).orElseThrow {
                    NotFoundException(
                        "Cannot find organization for authority",
                        EntityName.USER,
                        ErrorConstants.ERR_INVALID_AUTHORITY,
                        mapOf(
                            Pair("authorityName", role.authority),
                            Pair("projectId", organizationId.toString())
                        )
                    )
                }
            }
    }

    /**
     * Get or create given project role.
     * @param role to get or create
     * @param projectId organization ID
     * @return role from database
     */
    fun getProjectRole(role: RoleAuthority, projectId: Long): Role {
        return roleRepository.findOneByProjectIdAndAuthorityName(
            projectId, role.authority
        )
            ?: createNewRole(role) { r: Role ->
                r.project = projectRepository.findByIdWithOrganization(projectId) ?: throw NotFoundException(
                    "Cannot find project for authority", EntityName.USER, ErrorConstants.ERR_INVALID_AUTHORITY,
                    mapOf(
                        Pair("authorityName", role.authority),
                        Pair("projectId", projectId.toString())
                    )
                )
            }
    }

    /**
     * Get all roles related to a project.
     * @param projectName the project name
     * @return the roles
     */
    fun getRolesByProject(projectName: String): List<RoleDTO> {
        log.debug("Request to get all Roles for projectName $projectName")
        return roleRepository.findAllRolesByProjectName(projectName)
            .map { role: Role -> roleMapper.roleToRoleDTO(role) }.toList()
    }

    private fun getAuthority(role: RoleAuthority): Authority {
        return authorityRepository.findByAuthorityName(role.authority)
            ?: authorityRepository.saveAndFlush(Authority(role))
    }

    private fun createNewRole(role: RoleAuthority, apply: Consumer<Role>): Role {
        val newRole = Role()
        newRole.authority = getAuthority(role)
        apply.accept(newRole)
        return roleRepository.save(newRole)
    }

    /**
     * Get the role related to the given project with the given authority name.
     * @param projectName the project name
     * @param authorityName the authority name
     * @return an [Optional] containing the role if it exists, and empty otherwise
     */
    fun findOneByProjectNameAndAuthorityName(
        projectName: String?, authorityName: String?
    ): RoleDTO? {
        log.debug("Request to get role of project {} and authority {}", projectName, authorityName)
        return roleRepository.findOneByProjectNameAndAuthorityName(projectName, authorityName)
            .let { role -> role?.let { roleMapper.roleToRoleDTO(it) } }
    }

    companion object {
        private val log = LoggerFactory.getLogger(RoleService::class.java)

        /**
         * Get the predefined role authority from a RoleDTO.
         * @param roleDto roleDto to parse
         * @return role authority
         * @throws BadRequestException if the roleauthority is not found or does not correctly
         * specify an organization or project ID.
         */
        @JvmStatic
        fun getRoleAuthority(roleDto: RoleDTO): RoleAuthority {
            val authority: RoleAuthority
            authority = try {
                valueOfAuthority(roleDto.authorityName!!)
            } catch (ex: IllegalArgumentException) {
                throw BadRequestException(
                    "Authority not found with " + "authorityName",
                    EntityName.USER,
                    ErrorConstants.ERR_INVALID_AUTHORITY,
                    Collections.singletonMap(
                        "authorityName", roleDto.authorityName
                    )
                )
            }
            if (authority.scope === RoleAuthority.Scope.ORGANIZATION && roleDto.organizationId == null) {
                throw BadRequestException(
                    "Authority with " + "authorityName should have organization ID",
                    EntityName.USER,
                    ErrorConstants.ERR_INVALID_AUTHORITY,
                    Collections.singletonMap("authorityName", roleDto.authorityName)
                )
            }
            if (authority.scope === RoleAuthority.Scope.PROJECT && roleDto.projectId == null) {
                throw BadRequestException(
                    "Authority with " + "authorityName should have project ID",
                    EntityName.USER,
                    ErrorConstants.ERR_INVALID_AUTHORITY,
                    Collections.singletonMap("authorityName", roleDto.authorityName)
                )
            }
            return authority
        }
    }
}