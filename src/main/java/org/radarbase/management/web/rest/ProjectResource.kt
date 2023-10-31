package org.radarbase.management.web.rest

import io.micrometer.core.annotation.Timed
import io.swagger.v3.oas.annotations.Parameter
import org.radarbase.auth.authorization.EntityDetails
import org.radarbase.auth.authorization.Permission
import org.radarbase.management.domain.Subject
import org.radarbase.management.repository.ProjectRepository
import org.radarbase.management.security.Constants
import org.radarbase.management.security.NotAuthorizedException
import org.radarbase.management.service.AuthService
import org.radarbase.management.service.ProjectService
import org.radarbase.management.service.ResourceUriService
import org.radarbase.management.service.RoleService
import org.radarbase.management.service.SourceService
import org.radarbase.management.service.SubjectService
import org.radarbase.management.service.dto.ProjectDTO
import org.radarbase.management.service.dto.RoleDTO
import org.radarbase.management.service.dto.SourceTypeDTO
import org.radarbase.management.service.dto.SubjectDTO
import org.radarbase.management.service.mapper.SubjectMapper
import org.radarbase.management.web.rest.criteria.SubjectCriteria
import org.radarbase.management.web.rest.errors.BadRequestException
import org.radarbase.management.web.rest.errors.ErrorConstants
import org.radarbase.management.web.rest.errors.ErrorVM
import org.radarbase.management.web.rest.util.HeaderUtil.buildPath
import org.radarbase.management.web.rest.util.HeaderUtil.createEntityCreationAlert
import org.radarbase.management.web.rest.util.HeaderUtil.createEntityDeletionAlert
import org.radarbase.management.web.rest.util.HeaderUtil.createEntityUpdateAlert
import org.radarbase.management.web.rest.util.HeaderUtil.createFailureAlert
import org.radarbase.management.web.rest.util.PaginationUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.net.URISyntaxException
import javax.validation.Valid

/**
 * REST controller for managing Project.
 */
@RestController
@RequestMapping("/api")
class ProjectResource(
    @Autowired private val subjectMapper: SubjectMapper,
    @Autowired private val projectRepository: ProjectRepository,
    @Autowired private val projectService: ProjectService,
    @Autowired private val roleService: RoleService,
    @Autowired private val subjectService: SubjectService,
    @Autowired private val sourceService: SourceService,
    @Autowired private val authService: AuthService
) {

    /**
     * POST  /projects : Create a new project.
     *
     * @param projectDto the projectDto to create
     * @return the ResponseEntity with status 201 (Created) and with body the new projectDto, or
     * with status 400 (Bad Request) if the project has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/projects")
    @Timed
    @Throws(URISyntaxException::class, NotAuthorizedException::class)
    fun createProject(@RequestBody projectDto: @Valid ProjectDTO?): ResponseEntity<ProjectDTO> {
        log.debug("REST request to save Project : {}", projectDto)
        val org = projectDto!!.organization
        if (org?.name == null) {
            throw BadRequestException(
                "Organization must be provided",
                ENTITY_NAME, ErrorConstants.ERR_VALIDATION
            )
        }
        authService.checkPermission(
            Permission.PROJECT_CREATE,
            { e: EntityDetails -> e.organization(org.name) })
        if (projectDto.id != null) {
            return ResponseEntity.badRequest()
                .headers(
                    createFailureAlert(
                        ENTITY_NAME, "idexists", "A new project cannot already have an ID"
                    )
                )
                .body<ProjectDTO>(null)
        }
        if (projectRepository.findOneWithEagerRelationshipsByName(projectDto.projectName) != null) {
            return ResponseEntity.badRequest()
                .headers(
                    createFailureAlert(
                        ENTITY_NAME, "nameexists", "A project with this name already exists"
                    )
                )
                .body<ProjectDTO>(null)
        }
        val result = projectService.save(projectDto)
        return ResponseEntity.created(ResourceUriService.getUri(result))
            .headers(
                createEntityCreationAlert(
                    ENTITY_NAME,
                    result.projectName
                )
            )
            .body<ProjectDTO>(result)
    }

    /**
     * PUT  /projects : Updates an existing project.
     *
     * @param projectDto the projectDto to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated projectDto, or with
     * status 400 (Bad Request) if the projectDto is not valid, or with status 500 (Internal
     * Server Error) if the projectDto couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/projects")
    @Timed
    @Throws(URISyntaxException::class, NotAuthorizedException::class)
    fun updateProject(@RequestBody projectDto: @Valid ProjectDTO?): ResponseEntity<ProjectDTO> {log.debug("REST request to update Project : {}", projectDto)
        if (projectDto?.id == null) {
            return createProject(projectDto)
        }
        // When a client wants to link the project to the default organization,
        // this must be done explicitly.
        val org = projectDto.organization
        if (org?.name == null) {
            throw BadRequestException(
                "Organization must be provided",
                ENTITY_NAME, ErrorConstants.ERR_VALIDATION
            )
        }
        // When clients want to transfer a project,
        // they must have permissions to modify both new & old organizations
        val existingProject = projectService.findOne(projectDto.id!!)
        if (existingProject?.projectName != projectDto.projectName) {
            throw BadRequestException(
                "The project name cannot be modified.", ENTITY_NAME,
                ErrorConstants.ERR_VALIDATION
            )
        }
        val newOrgName = org.name
        authService.checkPermission(
            Permission.PROJECT_UPDATE,
            { e: EntityDetails ->
                e.organization(newOrgName)
                e.project(existingProject?.projectName)
            })
        val oldOrgName = existingProject?.organization?.name
        if (newOrgName != oldOrgName) {
            authService.checkPermission(
                Permission.PROJECT_UPDATE,
                { e: EntityDetails -> e.organization(oldOrgName) })
            authService.checkPermission(
                Permission.PROJECT_UPDATE,
                { e: EntityDetails -> e.organization(newOrgName) })
        }
        val result = projectService.save(projectDto)
        return ResponseEntity.ok()
            .headers(
                createEntityUpdateAlert(ENTITY_NAME, projectDto.projectName)
            )
            .body(result)
    }

    /**
     * GET  /projects : get all the projects.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of projects in body
     */
    @GetMapping("/projects")
    @Timed
    @Throws(NotAuthorizedException::class)
    fun getAllProjects(
        @PageableDefault(size = Int.MAX_VALUE) pageable: Pageable,
        @RequestParam(name = "minimized", required = false, defaultValue = "false") minimized: Boolean
    ): ResponseEntity<*> {
        log.debug("REST request to get Projects")
        authService.checkPermission(Permission.PROJECT_READ)
        val page = projectService.findAll(minimized, pageable)
        val headers = PaginationUtil
            .generatePaginationHttpHeaders(page, "/api/projects")
        return ResponseEntity(page.content, headers, HttpStatus.OK)
    }

    /**
     * GET  /projects/:projectName : get the project with this name.
     *
     * @param projectName the projectName of the projectDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the projectDTO, or with status
     * 404 (Not Found)
     */
    @GetMapping("/projects/{projectName:" + Constants.ENTITY_ID_REGEX + "}")
    @Timed
    @Throws(
        NotAuthorizedException::class
    )
    fun getProject(@PathVariable projectName: String?): ResponseEntity<ProjectDTO> {
        authService.checkScope(Permission.PROJECT_READ)
        log.debug("REST request to get Project : {}", projectName)
        val projectDto = projectService.findOneByName(projectName!!)
        authService.checkPermission(Permission.PROJECT_READ, { e: EntityDetails ->
            e.organization(projectDto.organization?.name)
            e.project(projectDto.projectName)
        })
        return ResponseEntity.ok(projectDto)
    }

    /**
     * GET  /projects/:projectName : get the "projectName" project.
     *
     * @param projectName the projectName of the projectDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the projectDTO, or with status
     * 404 (Not Found)
     */
    @GetMapping("/projects/{projectName:" + Constants.ENTITY_ID_REGEX + "}/source-types")
    @Timed
    @Throws(
        NotAuthorizedException::class
    )
    fun getSourceTypesOfProject(@PathVariable projectName: String?): List<SourceTypeDTO> {
        authService.checkScope(Permission.PROJECT_READ)
        log.debug("REST request to get Project : {}", projectName)
        val projectDto = projectService.findOneByName(projectName!!)
        authService.checkPermission(Permission.PROJECT_READ, { e: EntityDetails ->
            e.organization(projectDto.organization?.name)
            e.project(projectDto.projectName)
        })
        return projectService.findSourceTypesByProjectId(projectDto.id!!)
    }

    /**
     * DELETE  /projects/:projectName : delete the "projectName" project.
     *
     * @param projectName the projectName of the projectDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/projects/{projectName:" + Constants.ENTITY_ID_REGEX + "}")
    @Timed
    @Throws(
        NotAuthorizedException::class
    )
    fun deleteProject(@PathVariable projectName: String?): ResponseEntity<*> {
        authService.checkScope(Permission.PROJECT_DELETE)
        log.debug("REST request to delete Project : {}", projectName)
        val projectDto = projectService.findOneByName(projectName!!)
        authService.checkPermission(Permission.PROJECT_DELETE, { e: EntityDetails ->
            e.organization(projectDto.organization?.name)
            e.project(projectDto.projectName)
        })
        return try {
            projectService.delete(projectDto.id!!)
            ResponseEntity.ok()
                .headers(createEntityDeletionAlert(ENTITY_NAME, projectName))
                .build<Any>()
        } catch (ex: DataIntegrityViolationException) {
            ResponseEntity.badRequest()
                .body(ErrorVM(ErrorConstants.ERR_PROJECT_NOT_EMPTY, ex.message))
        }
    }

    /**
     * GET  /projects/{projectName}/roles : get all the roles created for this project.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of roles in body
     */
    @GetMapping("/projects/{projectName:" + Constants.ENTITY_ID_REGEX + "}/roles")
    @Timed
    @Throws(
        NotAuthorizedException::class
    )
    fun getRolesByProject(@PathVariable projectName: String?): ResponseEntity<List<RoleDTO>> {
        authService.checkScope(Permission.ROLE_READ)
        log.debug("REST request to get all Roles for project {}", projectName)
        val projectDto = projectService.findOneByName(projectName!!)
        authService.checkPermission(Permission.ROLE_READ, { e: EntityDetails ->
            e.organization(projectDto.organization?.name)
            e.project(projectDto.projectName)
        })
        return ResponseEntity.ok(roleService.getRolesByProject(projectName))
    }

    /**
     * GET  /projects/{projectName}/sources : get all the sources by project.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of sources in body
     */
    @GetMapping("/projects/{projectName:" + Constants.ENTITY_ID_REGEX + "}/sources")
    @Timed
    @Throws(
        NotAuthorizedException::class
    )
    fun getAllSourcesForProject(
        @Parameter pageable: Pageable,
        @PathVariable projectName: String,
        @RequestParam(value = "assigned", required = false) assigned: Boolean?,
        @RequestParam(name = "minimized", required = false, defaultValue = "false") minimized: Boolean
    ): ResponseEntity<*> {
        authService.checkScope(Permission.SOURCE_READ)
        log.debug("REST request to get all Sources")
        val projectDto = projectService.findOneByName(projectName) //?: throw NoSuchElementException()

        authService.checkPermission(Permission.SOURCE_READ, { e: EntityDetails ->
            e.organization(projectDto.organization?.name)
            e.project(projectDto.projectName)
        })
        return if (assigned != null) {
            if (minimized) {
                ResponseEntity.ok(
                    sourceService
                        .findAllMinimalSourceDetailsByProjectAndAssigned(
                            projectDto.id, assigned
                        )
                )
            } else {
                ResponseEntity.ok(
                    sourceService
                        .findAllByProjectAndAssigned(projectDto.id, assigned)
                )
            }
        } else {
            if (minimized) {
                val page = sourceService
                    .findAllMinimalSourceDetailsByProject(projectDto.id!!, pageable)
                val headers = PaginationUtil
                    .generatePaginationHttpHeaders(
                        page, buildPath(
                            "api",
                            "projects", projectName, "sources"
                        )
                    )
                ResponseEntity(page.content, headers, HttpStatus.OK)
            } else {
                val page = sourceService
                    .findAllByProjectId(projectDto.id!!, pageable)
                val headers = PaginationUtil
                    .generatePaginationHttpHeaders(
                        page, buildPath(
                            "api",
                            "projects", projectName, "sources"
                        )
                    )
                ResponseEntity(page.content, headers, HttpStatus.OK)
            }
        }
    }

    /**
     * Get /projects/{projectName}/subjects : get all subjects for a given project.
     *
     * @return The subjects in the project or 404 if there is no such project
     */
    @GetMapping("/projects/{projectName:" + Constants.ENTITY_ID_REGEX + "}/subjects")
    @Timed
    @Throws(
        NotAuthorizedException::class
    )
    fun getAllSubjects(
        subjectCriteria: @Valid SubjectCriteria?
    ): ResponseEntity<List<SubjectDTO?>> {
        authService.checkScope(Permission.SUBJECT_READ)


        val projectName = subjectCriteria!!.projectName ?: throw NoSuchElementException()
        // this checks if the project exists
        val projectDto = projectName.let { projectService.findOneByName(it) }
        authService.checkPermission(Permission.SUBJECT_READ, { e: EntityDetails ->
            e.organization(projectDto.organization?.name)
            e.project(projectDto.projectName)
        })

        // this checks if the project exists
        projectService.findOneByName(projectName)


        subjectCriteria.projectName = projectName
        log.debug(
            "REST request to get all subjects for project {} using criteria {}", projectName,
            subjectCriteria
        )
        val page = subjectService.findAll(subjectCriteria)
            .map { subject: Subject -> subjectMapper.subjectToSubjectWithoutProjectDTO(subject) }
        val baseUri = buildPath("api", "projects", projectName, "subjects")
        val headers = PaginationUtil.generateSubjectPaginationHttpHeaders(
            page, baseUri, subjectCriteria
        )
        return ResponseEntity(page.content, headers, HttpStatus.OK)
    }

    companion object {
        private val log = LoggerFactory.getLogger(ProjectResource::class.java)
        private const val ENTITY_NAME = "project"
    }
}
