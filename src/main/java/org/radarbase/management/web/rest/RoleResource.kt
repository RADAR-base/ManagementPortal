package org.radarbase.management.web.rest

import io.micrometer.core.annotation.Timed
import org.radarbase.auth.authorization.EntityDetails
import org.radarbase.auth.authorization.Permission
import org.radarbase.auth.authorization.RoleAuthority
import org.radarbase.management.security.Constants
import org.radarbase.management.security.NotAuthorizedException
import org.radarbase.management.service.AuthService
import org.radarbase.management.service.ResourceUriService
import org.radarbase.management.service.RoleService
import org.radarbase.management.service.dto.RoleDTO
import org.radarbase.management.web.rest.util.HeaderUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.security.access.annotation.Secured
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import tech.jhipster.web.util.ResponseUtil
import java.net.URISyntaxException
import java.util.*
import javax.validation.Valid

/**
 * REST controller for managing Role.
 */
@RestController
@RequestMapping("/api")
class RoleResource(
    @Autowired private val roleService: RoleService,
    @Autowired private val authService: AuthService
) {

    /**
     * POST  /Roles : Create a new role.
     *
     * @param roleDto the roleDto to create
     * @return the ResponseEntity with status 201 (Created) and with body the new RoleDTO, or with
     * status 400 (Bad Request) if the Role has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/roles")
    @Timed
    @Secured(RoleAuthority.SYS_ADMIN_AUTHORITY)
    @Throws(
        URISyntaxException::class, NotAuthorizedException::class
    )
    fun createRole(@RequestBody roleDto: @Valid RoleDTO): ResponseEntity<RoleDTO> {
        log.debug("REST request to save Role : {}", roleDto)
        authService.checkPermission(Permission.ROLE_CREATE, { e: EntityDetails ->
            e.project = roleDto.projectName
        })
        if (roleDto.id != null) {
            return ResponseEntity.badRequest().headers(
                HeaderUtil.createFailureAlert(
                    ENTITY_NAME,
                    "idexists", "A new role cannot already have an ID"
                )
            ).body(null)
        }
        val result = roleService.save(roleDto)
        return ResponseEntity.created(ResourceUriService.getUri(result))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, displayName(result)))
            .body(result)
    }

    /**
     * PUT  /roles : Updates an existing role.
     *
     * @param roleDto the roleDto to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated roleDto, or with
     * status 400 (Bad Request) if the roleDto is not valid, or with status 500 (Internal Server
     * Error) if the roleDto couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/roles")
    @Timed
    @Secured(RoleAuthority.SYS_ADMIN_AUTHORITY)
    @Throws(
        URISyntaxException::class, NotAuthorizedException::class
    )
    fun updateRole(@RequestBody roleDto: @Valid RoleDTO): ResponseEntity<RoleDTO> {
        log.debug("REST request to update Role : {}", roleDto)
        if (roleDto.id == null) {
            return createRole(roleDto)
        }
        authService.checkPermission(Permission.ROLE_UPDATE, { e: EntityDetails ->
            e.project = roleDto.projectName
        })
        val result = roleService.save(roleDto)
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, displayName(roleDto)))
            .body(result)
    }

    @get:Throws(NotAuthorizedException::class)
    @get:Timed
    @get:GetMapping("/roles")
    val allRoles: List<RoleDTO>
        /**
         * GET  /roles : get all the roles.
         *
         * @return the ResponseEntity with status 200 (OK) and the list of roles in body
         */
        get() {
            log.debug("REST request to get all Roles")
            authService.checkPermission(Permission.ROLE_READ)
            return roleService.findAll()
        }

    @get:Secured(RoleAuthority.SYS_ADMIN_AUTHORITY)
    @get:Timed
    @get:GetMapping("/roles/admin")
    val allAdminRoles: List<RoleDTO>
        /**
         * GET  /roles : get all the roles.
         *
         * @return the ResponseEntity with status 200 (OK) and the list of roles in body
         */
        get() {
            log.debug("REST request to get all Admin Roles")
            return roleService.findSuperAdminRoles()
        }

    /**
     * GET  /roles/:projectName/:authorityName : get the role of the specified project and
     * authority.
     *
     * @param projectName The project name
     * @param authorityName The authority name
     * @return the ResponseEntity with status 200 (OK) and with body the roleDTO, or with status 404
     * (Not Found)
     */
    @GetMapping(
        "/roles/{projectName:" + Constants.ENTITY_ID_REGEX + "}/{authorityName:"
                + Constants.ENTITY_ID_REGEX + "}"
    )
    @Timed
    @Throws(
        NotAuthorizedException::class
    )
    fun getRole(
        @PathVariable projectName: String?,
        @PathVariable authorityName: String?
    ): ResponseEntity<RoleDTO> {
        log.debug("REST request to get all Roles")
        authService.checkPermission(Permission.ROLE_READ, { e: EntityDetails -> e.project(projectName) })
        return ResponseUtil.wrapOrNotFound(
            Optional.ofNullable(roleService.findOneByProjectNameAndAuthorityName(projectName, authorityName))
        )
    }

    /**
     * Create a user-friendly display name for a given role. Useful for passing to the notification
     * system
     *
     * @param role The role to create a user-friendly display for
     * @return the display name
     */
    private fun displayName(role: RoleDTO?): String {
        return role?.projectName + ": " + role?.authorityName
    }

    companion object {
        private val log = LoggerFactory.getLogger(RoleResource::class.java)
        private const val ENTITY_NAME = "role"
    }
}
