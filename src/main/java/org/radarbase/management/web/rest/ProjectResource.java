package org.radarbase.management.web.rest;

import io.micrometer.core.annotation.Timed;
import io.swagger.annotations.ApiParam;
import org.radarbase.auth.config.Constants;
import org.radarbase.auth.exception.NotAuthorizedException;
import org.radarbase.auth.token.RadarToken;
import org.radarbase.management.repository.ProjectRepository;
import org.radarbase.management.repository.SubjectRepository;
import org.radarbase.management.service.ProjectService;
import org.radarbase.management.service.ResourceUriService;
import org.radarbase.management.service.RoleService;
import org.radarbase.management.service.SourceService;
import org.radarbase.management.service.dto.MinimalSourceDetailsDTO;
import org.radarbase.management.service.dto.ProjectDTO;
import org.radarbase.management.service.dto.RoleDTO;
import org.radarbase.management.service.dto.SourceDTO;
import org.radarbase.management.service.dto.SourceTypeDTO;
import org.radarbase.management.service.dto.SubjectDTO;
import org.radarbase.management.service.mapper.SubjectMapper;
import org.radarbase.management.web.rest.errors.ErrorVM;
import org.radarbase.management.web.rest.util.HeaderUtil;
import org.radarbase.management.web.rest.util.PaginationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.radarbase.auth.authorization.AuthoritiesConstants.INACTIVE_PARTICIPANT;
import static org.radarbase.auth.authorization.AuthoritiesConstants.PARTICIPANT;
import static org.radarbase.auth.authorization.Permission.PROJECT_CREATE;
import static org.radarbase.auth.authorization.Permission.PROJECT_DELETE;
import static org.radarbase.auth.authorization.Permission.PROJECT_READ;
import static org.radarbase.auth.authorization.Permission.PROJECT_UPDATE;
import static org.radarbase.auth.authorization.Permission.ROLE_READ;
import static org.radarbase.auth.authorization.Permission.SOURCE_READ;
import static org.radarbase.auth.authorization.Permission.SUBJECT_READ;
import static org.radarbase.auth.authorization.RadarAuthorization.checkPermission;
import static org.radarbase.auth.authorization.RadarAuthorization.checkPermissionOnProject;
import static org.radarbase.management.web.rest.errors.ErrorConstants.ERR_PROJECT_NOT_EMPTY;

/**
 * REST controller for managing Project.
 */
@RestController
@RequestMapping("/api")
public class ProjectResource {

    private static final Logger log = LoggerFactory.getLogger(ProjectResource.class);

    private static final String ENTITY_NAME = "project";

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private RadarToken token;

    @Autowired
    private RoleService roleService;

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private SubjectMapper subjectMapper;

    @Autowired
    private SourceService sourceService;

    /**
     * POST  /projects : Create a new project.
     *
     * @param projectDto the projectDto to create
     * @return the ResponseEntity with status 201 (Created) and with body the new projectDto, or
     *      with status 400 (Bad Request) if the project has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/projects")
    @Timed
    public ResponseEntity<ProjectDTO> createProject(@Valid @RequestBody ProjectDTO projectDto)
            throws URISyntaxException, NotAuthorizedException {
        log.debug("REST request to save Project : {}", projectDto);
        checkPermission(token, PROJECT_CREATE);
        if (projectDto.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(
                    ENTITY_NAME, "idexists", "A new project cannot already have an ID")).body(null);
        }
        if (projectRepository.findOneWithEagerRelationshipsByName(projectDto.getProjectName())
                .isPresent()) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(
                    ENTITY_NAME, "nameexists", "A project with this name already exists"))
                    .body(null);
        }
        ProjectDTO result = projectService.save(projectDto);
        return ResponseEntity.created(ResourceUriService.getUri(result))
                .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getProjectName()))
                .body(result);
    }

    /**
     * PUT  /projects : Updates an existing project.
     *
     * @param projectDto the projectDto to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated projectDto, or with
     *      status 400 (Bad Request) if the projectDto is not valid, or with status 500 (Internal
     *      Server Error) if the projectDto couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/projects")
    @Timed
    public ResponseEntity<ProjectDTO> updateProject(@Valid @RequestBody ProjectDTO projectDto)
            throws URISyntaxException, NotAuthorizedException {
        log.debug("REST request to update Project : {}", projectDto);
        if (projectDto.getId() == null) {
            return createProject(projectDto);
        }
        checkPermissionOnProject(token, PROJECT_UPDATE,
                projectDto.getProjectName());
        ProjectDTO result = projectService.save(projectDto);
        return ResponseEntity.ok()
                .headers(HeaderUtil
                        .createEntityUpdateAlert(ENTITY_NAME, projectDto.getProjectName()))
                .body(result);
    }

    /**
     * GET  /projects : get all the projects.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of projects in body
     */
    @GetMapping("/projects")
    @Timed
    public ResponseEntity<?> getAllProjects(
            @PageableDefault(page = 0, size = Integer.MAX_VALUE) Pageable pageable,
            @RequestParam(name = "minimized", required = false, defaultValue = "false") Boolean
                    minimized) throws NotAuthorizedException {
        log.debug("REST request to get Projects");
        checkPermission(token, PROJECT_READ);
        Page<?> page = projectService.findAll(minimized, pageable);
        HttpHeaders headers = PaginationUtil
                .generatePaginationHttpHeaders(page, "/api/projects");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /projects/:projectName : get the project with this name.
     *
     * @param projectName the projectName of the projectDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the projectDTO, or with status
     *      404 (Not Found)
     */
    @GetMapping("/projects/{projectName:" + Constants.ENTITY_ID_REGEX + "}")
    @Timed
    public ResponseEntity<ProjectDTO> getProject(@PathVariable String projectName)
            throws NotAuthorizedException {
        log.debug("REST request to get Project : {}", projectName);
        ProjectDTO projectDto = projectService.findOneByName(projectName);
        checkPermissionOnProject(token, PROJECT_READ, projectDto.getProjectName());
        return ResponseEntity.ok(projectDto);
    }

    /**
     * GET  /projects/:projectName : get the "projectName" project.
     *
     * @param projectName the projectName of the projectDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the projectDTO, or with status
     *      404 (Not Found)
     */
    @GetMapping("/projects/{projectName:" + Constants.ENTITY_ID_REGEX + "}/source-types")
    @Timed
    public List<SourceTypeDTO> getSourceTypesOfProject(@PathVariable String projectName)
            throws NotAuthorizedException {
        log.debug("REST request to get Project : {}", projectName);
        ProjectDTO projectDto = projectService.findOneByName(projectName);
        checkPermissionOnProject(token, PROJECT_READ, projectDto.getProjectName());
        return projectService.findSourceTypesByProjectId(projectDto.getId());
    }


    /**
     * DELETE  /projects/:projectName : delete the "projectName" project.
     *
     * @param projectName the projectName of the projectDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/projects/{projectName:" + Constants.ENTITY_ID_REGEX + "}")
    @Timed
    public ResponseEntity<?> deleteProject(@PathVariable String projectName)
            throws NotAuthorizedException {
        log.debug("REST request to delete Project : {}", projectName);
        ProjectDTO projectDto = projectService.findOneByName(projectName);
        checkPermissionOnProject(token, PROJECT_DELETE,
                projectDto.getProjectName());

        try {
            projectService.delete(projectDto.getId());
            return ResponseEntity.ok()
                    .headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, projectName))
                    .build();
        } catch (DataIntegrityViolationException ex) {
            return ResponseEntity.badRequest()
                    .body(new ErrorVM(ERR_PROJECT_NOT_EMPTY, ex.getMessage()));
        }
    }

    /**
     * GET  /projects/{projectName}/roles : get all the roles created for this project.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of roles in body
     */
    @GetMapping("/projects/{projectName:" + Constants.ENTITY_ID_REGEX + "}/roles")
    @Timed
    public ResponseEntity<List<RoleDTO>> getRolesByProject(@PathVariable String projectName)
            throws NotAuthorizedException {
        log.debug("REST request to get all Roles for project {}", projectName);
        ProjectDTO projectDto = projectService.findOneByName(projectName);
        checkPermissionOnProject(token, ROLE_READ, projectDto.getProjectName());
        return ResponseEntity.ok(roleService.getRolesByProject(projectName));
    }

    /**
     * GET  /projects/{projectName}/sources : get all the sources by project.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of sources in body
     */
    @GetMapping("/projects/{projectName:" + Constants.ENTITY_ID_REGEX + "}/sources")
    @Timed
    public ResponseEntity<?> getAllSourcesForProject(@ApiParam Pageable pageable,
            @PathVariable String projectName,
            @RequestParam(value = "assigned", required = false) Boolean assigned,
            @RequestParam(name = "minimized", required = false, defaultValue = "false")
                    Boolean minimized) throws NotAuthorizedException {
        log.debug("REST request to get all Sources");
        ProjectDTO projectDto = projectService.findOneByName(projectName);
        RadarToken jwt = token;
        checkPermissionOnProject(jwt, SOURCE_READ, projectDto.getProjectName());
        if (!jwt.isClientCredentials() && jwt.hasAuthority(PARTICIPANT)) {
            throw new NotAuthorizedException("Cannot list all project sources as a participant.");
        }

        if (Objects.nonNull(assigned)) {
            if (minimized) {
                return ResponseEntity.ok(sourceService
                        .findAllMinimalSourceDetailsByProjectAndAssigned(
                                projectDto.getId(), assigned));
            } else {
                return ResponseEntity.ok(sourceService
                        .findAllByProjectAndAssigned(projectDto.getId(), assigned));
            }
        } else {
            if (minimized) {
                Page<MinimalSourceDetailsDTO> page = sourceService
                        .findAllMinimalSourceDetailsByProject(projectDto.getId(), pageable);
                HttpHeaders headers = PaginationUtil
                        .generatePaginationHttpHeaders(page, HeaderUtil.buildPath("api",
                                "projects", projectName, "sources"));
                return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
            } else {
                Page<SourceDTO> page = sourceService
                        .findAllByProjectId(projectDto.getId(), pageable);
                HttpHeaders headers = PaginationUtil
                        .generatePaginationHttpHeaders(page, HeaderUtil.buildPath("api",
                                "projects", projectName, "sources"));
                return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
            }
        }
    }

    /**
     * Get /projects/{projectName}/subjects : get all subjects for a given project.
     *
     * @param projectName The name of the project
     * @return The subjects in the project or 404 if there is no such project
     */
    @GetMapping("/projects/{projectName:" + Constants.ENTITY_ID_REGEX + "}/subjects")
    @Timed
    public ResponseEntity<List<SubjectDTO>> getAllSubjects(@ApiParam Pageable pageable,
            @PathVariable String projectName,
            @RequestParam(value = "withInactiveParticipants", required = false)
                    Boolean inactiveParticipantsParam) throws NotAuthorizedException {
        // this checks if the project exists
        projectService.findOneByName(projectName);
        RadarToken jwt = token;
        checkPermissionOnProject(jwt, SUBJECT_READ, projectName);
        if (!jwt.isClientCredentials() && jwt.hasAuthority(PARTICIPANT)) {
            throw new NotAuthorizedException("Cannot list all project subjects as a participant.");
        }

        log.debug("REST request to get all subjects for project {}", projectName);
        Page<SubjectDTO> page;
        boolean includeInactiveParticipants = inactiveParticipantsParam != null
                && inactiveParticipantsParam;
        if (includeInactiveParticipants) {
            page = subjectRepository.findAllByProjectNameAndAuthoritiesIn(pageable, projectName,
                    Arrays.asList(PARTICIPANT, INACTIVE_PARTICIPANT))
                    .map(subjectMapper::subjectToSubjectWithoutProjectDTO);

        } else {
            page = subjectRepository.findAllByProjectNameAndAuthoritiesIn(pageable, projectName,
                    Collections.singletonList(PARTICIPANT))
                    .map(subjectMapper::subjectToSubjectWithoutProjectDTO);
        }

        HttpHeaders headers = PaginationUtil
                .generatePaginationHttpHeaders(page, HeaderUtil.buildPath("api",
                        "projects", projectName, "subjects"));
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }
}
