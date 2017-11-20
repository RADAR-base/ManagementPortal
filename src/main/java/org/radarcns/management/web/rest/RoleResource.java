package org.radarcns.management.web.rest;

import com.codahale.metrics.annotation.Timed;
import io.github.jhipster.web.util.ResponseUtil;
import org.radarcns.auth.authorization.AuthoritiesConstants;
import org.radarcns.management.service.RoleService;
import org.radarcns.management.service.dto.RoleDTO;
import org.radarcns.management.web.rest.util.HeaderUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

import static org.radarcns.auth.authorization.Permission.ROLE_CREATE;
import static org.radarcns.auth.authorization.Permission.ROLE_READ;
import static org.radarcns.auth.authorization.Permission.ROLE_UPDATE;
import static org.radarcns.auth.authorization.RadarAuthorization.checkPermissionOnProject;
import static org.radarcns.management.security.SecurityUtils.getJWT;

/**
 * REST controller for managing Role.
 */
@RestController
@RequestMapping("/api")
public class RoleResource {

    private final Logger log = LoggerFactory.getLogger(RoleResource.class);

    private static final String ENTITY_NAME = "role";

    @Autowired
    private RoleService roleService;

    @Autowired
    private HttpServletRequest servletRequest;

    /**
     * POST  /Roles : Create a new role.
     *
     * @param roleDTO the roleDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the new RoleDTO, or with status 400 (Bad Request) if the Role has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/roles")
    @Timed
    public ResponseEntity<RoleDTO> createRole(@Valid @RequestBody RoleDTO roleDTO) throws URISyntaxException {
        log.debug("REST request to save Role : {}", roleDTO);
        checkPermissionOnProject(getJWT(servletRequest), ROLE_CREATE, roleDTO.getProjectName());
        if (roleDTO.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "idexists", "A new role cannot already have an ID")).body(null);
        }
        RoleDTO result = roleService.save(roleDTO);
        return ResponseEntity.created(new URI("/api/roles/" + result.getProjectName() + "/" +
                result.getAuthorityName()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getProjectName() + "/" +
                    result.getAuthorityName()))
            .body(result);
    }

    /**
     * PUT  /roles : Updates an existing role.
     *
     * @param roleDTO the roleDTO to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated roleDTO,
     * or with status 400 (Bad Request) if the roleDTO is not valid,
     * or with status 500 (Internal Server Error) if the roleDTO couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/roles")
    @Timed
    public ResponseEntity<RoleDTO> updateRole(@Valid @RequestBody RoleDTO roleDTO) throws URISyntaxException {
        log.debug("REST request to update Role : {}", roleDTO);
        if (roleDTO.getId() == null) {
            return createRole(roleDTO);
        }
        checkPermissionOnProject(getJWT(servletRequest), ROLE_UPDATE, roleDTO.getProjectName());
        RoleDTO result = roleService.save(roleDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, roleDTO.getId().toString()))
            .body(result);
    }

    /**
     * GET  /roles : get all the roles.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of roles in body
     */
    @GetMapping("/roles")
    @Timed
    @Secured( {AuthoritiesConstants.PROJECT_ADMIN, AuthoritiesConstants.SYS_ADMIN})
    public List<RoleDTO> getAllRoles() {
        log.debug("REST request to get all Roles");
        return roleService.findAll();
    }

    /**
     * GET  /roles : get all the roles.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of roles in body
     */
    @GetMapping("/roles/admin")
    @Timed
    public List<RoleDTO> getAllAdminRoles() {
        log.debug("REST request to get all Admin Roles");
        return roleService.findSuperAdminRoles();
    }

    /**
     * GET  /roles/:projectName/:authorityName : get the role of the specified project and authority
     *
     * @param projectName The project name
     * @param authorityName The authority name
     * @return the ResponseEntity with status 200 (OK) and with body the roleDTO, or with status 404 (Not Found)
     */
    @GetMapping("/roles/{projectName}/{authorityName}")
    @Timed
    public ResponseEntity<RoleDTO> getRole(@PathVariable String projectName,
        @PathVariable String authorityName) {
        checkPermissionOnProject(getJWT(servletRequest), ROLE_READ, projectName);
        return ResponseUtil.wrapOrNotFound(roleService
            .findOneByProjectNameAndAuthorityName(projectName, authorityName));
    }

}
