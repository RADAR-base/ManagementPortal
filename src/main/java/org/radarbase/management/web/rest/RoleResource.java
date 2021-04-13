package org.radarbase.management.web.rest;

import com.codahale.metrics.annotation.Timed;
import io.github.jhipster.web.util.ResponseUtil;
import org.radarbase.auth.authorization.AuthoritiesConstants;
import org.radarbase.auth.config.Constants;
import org.radarbase.auth.exception.NotAuthorizedException;
import org.radarbase.management.service.ResourceUriService;
import org.radarbase.management.service.RoleService;
import org.radarbase.management.service.dto.RoleDTO;
import org.radarbase.management.web.rest.util.HeaderUtil;
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
import java.net.URISyntaxException;
import java.util.List;

import static org.radarbase.auth.authorization.Permission.ROLE_CREATE;
import static org.radarbase.auth.authorization.Permission.ROLE_READ;
import static org.radarbase.auth.authorization.Permission.ROLE_UPDATE;
import static org.radarbase.auth.authorization.RadarAuthorization.checkPermissionOnProject;
import static org.radarbase.management.security.SecurityUtils.getJWT;

/**
 * REST controller for managing Role.
 */
@RestController
@RequestMapping("/api")
public class RoleResource {

    private static final Logger log = LoggerFactory.getLogger(RoleResource.class);

    private static final String ENTITY_NAME = "role";

    @Autowired
    private RoleService roleService;

    @Autowired
    private HttpServletRequest servletRequest;

    /**
     * POST  /Roles : Create a new role.
     *
     * @param roleDto the roleDto to create
     * @return the ResponseEntity with status 201 (Created) and with body the new RoleDTO, or with
     *     status 400 (Bad Request) if the Role has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/roles")
    @Timed
    public ResponseEntity<RoleDTO> createRole(@Valid @RequestBody RoleDTO roleDto)
            throws URISyntaxException, NotAuthorizedException {
        log.debug("REST request to save Role : {}", roleDto);
        checkPermissionOnProject(getJWT(servletRequest), ROLE_CREATE, roleDto.getProjectName());
        if (roleDto.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME,
                    "idexists", "A new role cannot already have an ID")).body(null);
        }
        RoleDTO result = roleService.save(roleDto);
        return ResponseEntity.created(ResourceUriService.getUri(result))
                .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, displayName(result)))
                .body(result);
    }

    /**
     * PUT  /roles : Updates an existing role.
     *
     * @param roleDto the roleDto to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated roleDto, or with
     *     status 400 (Bad Request) if the roleDto is not valid, or with status 500 (Internal Server
     *     Error) if the roleDto couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/roles")
    @Timed
    public ResponseEntity<RoleDTO> updateRole(@Valid @RequestBody RoleDTO roleDto)
            throws URISyntaxException, NotAuthorizedException {
        log.debug("REST request to update Role : {}", roleDto);
        if (roleDto.getId() == null) {
            return createRole(roleDto);
        }
        checkPermissionOnProject(getJWT(servletRequest), ROLE_UPDATE, roleDto.getProjectName());
        RoleDTO result = roleService.save(roleDto);
        return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, displayName(roleDto)))
                .body(result);
    }

    /**
     * GET  /roles : get all the roles.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of roles in body
     */
    @GetMapping("/roles")
    @Timed
    @Secured({AuthoritiesConstants.PROJECT_ADMIN, AuthoritiesConstants.SYS_ADMIN})
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
     * GET  /roles/:projectName/:authorityName : get the role of the specified project and
     * authority.
     *
     * @param projectName The project name
     * @param authorityName The authority name
     * @return the ResponseEntity with status 200 (OK) and with body the roleDTO, or with status 404
     *     (Not Found)
     */
    @GetMapping("/roles/{projectName:" + Constants.ENTITY_ID_REGEX + "}/{authorityName:"
            + Constants.ENTITY_ID_REGEX + "}")
    @Timed
    public ResponseEntity<RoleDTO> getRole(@PathVariable String projectName,
            @PathVariable String authorityName) throws NotAuthorizedException {
        checkPermissionOnProject(getJWT(servletRequest), ROLE_READ, projectName);
        return ResponseUtil.wrapOrNotFound(roleService
                .findOneByProjectNameAndAuthorityName(projectName, authorityName));
    }

    /**
     * Create a user-friendly display name for a given role. Useful for passing to the notification
     * system
     *
     * @param role The role to create a user-friendly display for
     * @return the display name
     */
    private String displayName(RoleDTO role) {
        return role.getProjectName() + ": " + role.getAuthorityName();
    }
}
