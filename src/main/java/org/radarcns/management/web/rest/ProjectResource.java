package org.radarcns.management.web.rest;

import com.codahale.metrics.annotation.Timed;
import io.github.jhipster.web.util.ResponseUtil;
import org.radarcns.auth.authorization.AuthoritiesConstants;
import org.radarcns.management.service.ProjectService;
import org.radarcns.management.service.RoleService;
import org.radarcns.management.service.SourceService;
import org.radarcns.management.service.dto.DeviceTypeDTO;
import org.radarcns.management.service.dto.MinimalSourceDetailsDTO;
import org.radarcns.management.service.dto.ProjectDTO;
import org.radarcns.management.service.dto.RoleDTO;
import org.radarcns.management.web.rest.util.HeaderUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

import static org.radarcns.auth.authorization.Permission.PROJECT_CREATE;
import static org.radarcns.auth.authorization.Permission.PROJECT_DELETE;
import static org.radarcns.auth.authorization.Permission.PROJECT_READ;
import static org.radarcns.auth.authorization.Permission.PROJECT_UPDATE;
import static org.radarcns.auth.authorization.Permission.ROLE_READ;
import static org.radarcns.auth.authorization.Permission.SOURCE_READ;
import static org.radarcns.auth.authorization.RadarAuthorization.checkPermission;
import static org.radarcns.auth.authorization.RadarAuthorization.checkPermissionOnProject;
import static org.radarcns.management.security.SecurityUtils.getJWT;

/**
 * REST controller for managing Project.
 */
@RestController
@RequestMapping("/api")
public class ProjectResource {

    private final Logger log = LoggerFactory.getLogger(ProjectResource.class);

    private static final String ENTITY_NAME = "project";

    @Autowired
    private ProjectService projectService;

    @Autowired
    private HttpServletRequest servletRequest;

    @Autowired
    private RoleService roleService;

    @Autowired
    private SourceService sourceService;

    /**
     * POST  /projects : Create a new project.
     *
     * @param projectDTO the projectDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the new projectDTO, or with status 400 (Bad Request) if the project has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/projects")
    @Timed
    public ResponseEntity<ProjectDTO> createProject(@Valid @RequestBody ProjectDTO projectDTO) throws URISyntaxException {
        log.debug("REST request to save Project : {}", projectDTO);
        checkPermission(getJWT(servletRequest), PROJECT_CREATE);
        if (projectDTO.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "idexists", "A new project cannot already have an ID")).body(null);
        }
        ProjectDTO result = projectService.save(projectDTO);
        return ResponseEntity.created(new URI("/api/projects/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /projects : Updates an existing project.
     *
     * @param projectDTO the projectDTO to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated projectDTO,
     * or with status 400 (Bad Request) if the projectDTO is not valid,
     * or with status 500 (Internal Server Error) if the projectDTO couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/projects")
    @Timed
    public ResponseEntity<ProjectDTO> updateProject(@Valid @RequestBody ProjectDTO projectDTO) throws URISyntaxException {
        log.debug("REST request to update Project : {}", projectDTO);
        if (projectDTO.getId() == null) {
            return createProject(projectDTO);
        }
        checkPermissionOnProject(getJWT(servletRequest), PROJECT_UPDATE, projectDTO.getProjectName());
        ProjectDTO result = projectService.save(projectDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, projectDTO.getId().toString()))
            .body(result);
    }

    /**
     * GET  /projects : get all the projects.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of projects in body
     */
    @GetMapping("/projects")
    @Timed
    public List<ProjectDTO> getAllProjects(
            @RequestParam(name = "minimized", required = false, defaultValue = "false") Boolean
                minimized) {
        log.debug("REST request to get Projects");
        checkPermission(getJWT(servletRequest), PROJECT_READ);
        return projectService.findAll(minimized);
    }

    /**
     * GET  /projects/:id : get the "id" project.
     *
     * @param id the id of the projectDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the projectDTO, or with status 404 (Not Found)
     */
    @GetMapping("/projects/{id}")
    @Timed
    public ResponseEntity<ProjectDTO> getProject(@PathVariable Long id) {
        log.debug("REST request to get Project : {}", id);
        ProjectDTO projectDTO = projectService.findOne(id);
        if (projectDTO != null) {
            checkPermissionOnProject(getJWT(servletRequest), PROJECT_READ, projectDTO.getProjectName());
        }
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(projectDTO));
    }

    /**
     * GET  /projects/:id : get the "id" project.
     *
     * @param id the id of the projectDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the projectDTO, or with status 404 (Not Found)
     */
    @GetMapping("/projects/{id}/device-types")
    @Timed
    public List<DeviceTypeDTO> getDeviceTypesOfProject(@PathVariable Long id) {
        log.debug("REST request to get Project : {}", id);
        ProjectDTO projectDTO = projectService.findOne(id);
        if (projectDTO != null) {
            checkPermissionOnProject(getJWT(servletRequest), PROJECT_READ, projectDTO.getProjectName());
        }
        return projectService.findDeviceTypesById(id);
    }


    /**
     * DELETE  /projects/:id : delete the "id" project.
     *
     * @param id the id of the projectDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/projects/{id}")
    @Timed
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        log.debug("REST request to delete Project : {}", id);
        ProjectDTO projectDTO = projectService.findOne(id);
        if (projectDTO != null) {
            checkPermissionOnProject(getJWT(servletRequest), PROJECT_DELETE, projectDTO.getProjectName());
        }
        projectService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

    /**
     * GET  /projects/{id}/roles : get all the roles created for this project.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of roles in body
     */
    @GetMapping("/projects/{id}/roles")
    @Timed
    public List<RoleDTO> getRolesByProject(@PathVariable Long id) {
        log.debug("REST request to get all Roles for this project");
        ProjectDTO projectDTO = projectService.findOne(id);
        if (projectDTO != null) {
            checkPermissionOnProject(getJWT(servletRequest), ROLE_READ, projectDTO.getProjectName());
        }
        return roleService.getRolesByProject(id);
    }

    /**
     * GET  /projects/{id}/sources : get all the sources by project
     *
     * @return the ResponseEntity with status 200 (OK) and the list of sources in body
     */
    @GetMapping("/projects/{id}/sources")
    @Timed
    public List<MinimalSourceDetailsDTO> getAllSourcesForProject(@PathVariable Long id,
            @RequestParam(value = "assigned", required = false) Boolean assigned) {
        log.debug("REST request to get all Sources");
        ProjectDTO projectDTO = projectService.findOne(id);
        if (projectDTO != null) {
            checkPermissionOnProject(getJWT(servletRequest), SOURCE_READ, projectDTO.getProjectName());
        }
        if(assigned !=null) {
            return sourceService.findAllByProjectAndAssigned(id, assigned);
        }
        return sourceService.findAllMinimalSourceDetailsByProject(id);
    }

}
