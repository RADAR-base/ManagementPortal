package org.radarbase.management.web.rest

import io.micrometer.core.annotation.Timed
import org.radarbase.auth.authorization.EntityDetails
import org.radarbase.auth.authorization.Permission
import org.radarbase.management.security.Constants
import org.radarbase.management.security.NotAuthorizedException
import org.radarbase.management.service.AuthService
import org.radarbase.management.service.OrganizationService
import org.radarbase.management.service.ResourceUriService.getUri
import org.radarbase.management.service.dto.OrganizationDTO
import org.radarbase.management.service.dto.ProjectDTO
import org.radarbase.management.web.rest.errors.EntityName
import org.radarbase.management.web.rest.errors.ErrorConstants
import org.radarbase.management.web.rest.errors.NotFoundException
import org.radarbase.management.web.rest.util.HeaderUtil.createEntityCreationAlert
import org.radarbase.management.web.rest.util.HeaderUtil.createFailureAlert
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URISyntaxException
import java.util.*
import jakarta.validation.Valid

/**
 * REST controller for managing Organization.
 */
@RestController
@RequestMapping("/api")
class OrganizationResource(
    @Autowired private val organizationService: OrganizationService, @Autowired private val authService: AuthService
) {

    /**
     * POST  /organizations : Create a new organization.
     *
     * @param organizationDto the organizationDto to create
     * @return the ResponseEntity with status 201 (Created)
     * and with body the new organizationDto,
     * or with status 400 (Bad Request) if the organization already has an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/organizations")
    @Timed
    @Throws(URISyntaxException::class, NotAuthorizedException::class)
    fun createOrganization(
        @RequestBody @Valid organizationDto: OrganizationDTO?
    ): ResponseEntity<OrganizationDTO?> {
        log.debug("REST request to save Organization : {}", organizationDto)
        authService.checkPermission(Permission.ORGANIZATION_CREATE)
        if (organizationDto?.id != null) {
            val msg = "A new organization cannot already have an ID"
            val headers = createFailureAlert(ENTITY_NAME, "idexists", msg)
            return ResponseEntity.badRequest().headers(headers).body(null)
        }
        val existingOrg = organizationDto?.name?.let { organizationService.findByName(it) }
        if (existingOrg != null) {
            val msg = "An organization with this name already exists"
            val headers = createFailureAlert(ENTITY_NAME, "nameexists", msg)
            return ResponseEntity.status(HttpStatus.CONFLICT).headers(headers).body(null)
        }
        val result = organizationDto?.let { organizationService.save(it) }
        return result?.let { getUri(it) }?.let {
            ResponseEntity.created(it).headers(createEntityCreationAlert(ENTITY_NAME, result.name)).body(result)
        }
            //TODO handle better
            ?: ResponseEntity.badRequest().body(null)
    }

    /**
     * GET  /organizations : get all the organizations.
     *
     * @return the ResponseEntity with status 200 (OK)
     * and the list of organizations in body
     */
    @Throws(NotAuthorizedException::class)
    @Timed
    @GetMapping("/organizations")
    fun allOrganizations(): ResponseEntity<*> {
        log.debug("REST request to get Organizations")
        authService.checkScope(Permission.ORGANIZATION_READ)
        val orgs = organizationService.findAll()
        return ResponseEntity(orgs, HttpStatus.OK)
    }

    /**
     * PUT  /organizations : Updates an existing organization.
     *
     * @param organizationDto the organizationDto to update
     * @return the ResponseEntity
     * with status 200 and with the updated organizationDto as body,
     * or with status 400 if the organizationDto is not valid,
     * or with status 500 if the organizationDto couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/organizations")
    @Timed
    @Throws(URISyntaxException::class, NotAuthorizedException::class)
    fun updateOrganization(
        @RequestBody @Valid organizationDto: OrganizationDTO?
    ): ResponseEntity<OrganizationDTO?> {
        log.debug("REST request to update Organization : {}", organizationDto)
        if (organizationDto!!.id == null) {
            return createOrganization(organizationDto)
        }
        val name = organizationDto.name
        authService.checkPermission(Permission.ORGANIZATION_UPDATE, { e: EntityDetails -> e.organization(name) })
        val result = organizationService.save(organizationDto)
        return ResponseEntity.ok().headers(createEntityCreationAlert(ENTITY_NAME, result.name)).body(result)
    }

    /**
     * GET  /organizations/:organizationName : get the organization with this name.
     *
     * @param name the name of the organizationDTO to retrieve
     * @return the ResponseEntity with status 200 (OK)
     * and with body the organizationDTO,
     * or with status 404 (Not Found)
     */
    @GetMapping("/organizations/{name:" + Constants.ENTITY_ID_REGEX + "}")
    @Timed
    @Throws(
        NotAuthorizedException::class
    )
    fun getOrganization(
        @PathVariable name: String
    ): ResponseEntity<OrganizationDTO> {
        log.debug("REST request to get Organization : {}", name)
        authService.checkPermission(Permission.ORGANIZATION_READ, { e: EntityDetails -> e.organization(name) })
        val org = organizationService.findByName(name)
        val dto = org ?: throw NotFoundException(
            "Organization not found with name $name",
            EntityName.ORGANIZATION,
            ErrorConstants.ERR_ORGANIZATION_NAME_NOT_FOUND,
            Collections.singletonMap("name", name)
        )
        return ResponseEntity.ok(dto)
    }

    /**
     * GET  /organizations/:organizationName/projects
     * : get projects belonging to the organization with this name.
     *
     * @param name the name of the organization
     * @return the ResponseEntity
     * with status 200 (OK) and with body containing the project list,
     * or with status 404 (Not Found)
     */
    @GetMapping("/organizations/{name:" + Constants.ENTITY_ID_REGEX + "}/projects")
    @Timed
    @Throws(
        NotAuthorizedException::class
    )
    fun getOrganizationProjects(
        @PathVariable name: String?
    ): ResponseEntity<List<ProjectDTO>> {
        log.debug("REST request to get Projects of the Organization : {}", name)
        authService.checkPermission(Permission.PROJECT_READ, { e: EntityDetails -> e.organization(name) })
        val projects = name?.let { organizationService.findAllProjectsByOrganizationName(it) }
        return ResponseEntity.ok(projects)
    }

    companion object {
        private val log = LoggerFactory.getLogger(OrganizationResource::class.java)
        private const val ENTITY_NAME = "organization"
    }
}
