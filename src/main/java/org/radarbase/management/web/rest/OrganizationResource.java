package org.radarbase.management.web.rest;

import io.micrometer.core.annotation.Timed;
import org.radarbase.auth.config.Constants;
import org.radarbase.auth.exception.NotAuthorizedException;
// import org.radarbase.auth.token.RadarToken;
import org.radarbase.auth.token.RadarToken;
import org.radarbase.management.service.OrganizationService;
import org.radarbase.management.service.ResourceUriService;
import org.radarbase.management.service.dto.OrganizationDTO;
import org.radarbase.management.service.dto.ProjectDTO;
import org.radarbase.management.web.rest.errors.NotFoundException;
import org.radarbase.management.web.rest.errors.ErrorConstants;
import org.radarbase.management.web.rest.util.HeaderUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;

import static org.radarbase.auth.authorization.Permission.ORGANIZATION_CREATE;
import static org.radarbase.auth.authorization.Permission.ORGANIZATION_READ;
import static org.radarbase.auth.authorization.Permission.ORGANIZATION_UPDATE;
import static org.radarbase.auth.authorization.RadarAuthorization.checkPermission;
import static org.radarbase.auth.authorization.RadarAuthorization.checkPermissionOnOrganization;
import static org.radarbase.management.web.rest.errors.EntityName.ORGANIZATION;
/*
import static org.radarbase.auth.authorization.Permission.ORGANIZATION_CREATE;
import static org.radarbase.auth.authorization.Permission.ORGANIZATION_READ;
import static org.radarbase.auth.authorization.Permission.ORGANIZATION_UPDATE;
import static org.radarbase.auth.authorization.RadarAuthorization.checkPermission;
import static org.radarbase.auth.authorization.RadarAuthorization.checkPermissionOnOrganization;
*/

/**
 * REST controller for managing Organization.
 */
@RestController
@RequestMapping("/api")
public class OrganizationResource {

    private static final Logger log = LoggerFactory.getLogger(OrganizationResource.class);

    private static final String ENTITY_NAME = "organization";

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private RadarToken token;

    /**
     * POST  /organizations : Create a new organization.
     *
     * @param organizationDto the organizationDto to create
     * @return the ResponseEntity with status 201 (Created)
     *      and with body the new organizationDto,
     *      or with status 400 (Bad Request) if the organization already has an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/organizations")
    @Timed
    public ResponseEntity<OrganizationDTO> createOrganization(
            @Valid @RequestBody OrganizationDTO organizationDto
    ) throws URISyntaxException, NotAuthorizedException {
        log.debug("REST request to save Organization : {}", organizationDto);
        checkPermission(token, ORGANIZATION_CREATE);
        if (organizationDto.getId() != null) {
            var msg = "A new organization cannot already have an ID";
            var headers = HeaderUtil.createFailureAlert(ENTITY_NAME, "idexists", msg);
            return ResponseEntity.badRequest().headers(headers).body(null);
        }
        var existingOrg = organizationService.findByName(organizationDto.getName());
        if (existingOrg.isPresent()) {
            var msg = "An organization with this name already exists";
            var headers = HeaderUtil.createFailureAlert(ENTITY_NAME, "nameexists", msg);
            return ResponseEntity.status(HttpStatus.CONFLICT).headers(headers).body(null);
        }
        var result = organizationService.save(organizationDto);
        return ResponseEntity.created(ResourceUriService.getUri(result))
                .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getName()))
                .body(result);
    }

    /**
     * GET  /organizations : get all the organizations.
     *
     * @return the ResponseEntity with status 200 (OK)
     *      and the list of organizations in body
     */
    @GetMapping("/organizations")
    @Timed
    public ResponseEntity<?> getAllOrganizations() throws NotAuthorizedException {
        log.debug("REST request to get Organizations");
        checkPermission(token, ORGANIZATION_READ);
        var orgs = organizationService.findAll();
        return new ResponseEntity<>(orgs, HttpStatus.OK);
    }

    /**
     * PUT  /organizations : Updates an existing organization.
     *
     * @param organizationDto the organizationDto to update
     * @return the ResponseEntity
     *      with status 200 and with the updated organizationDto as body,
     *      or with status 400 if the organizationDto is not valid,
     *      or with status 500 if the organizationDto couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/organizations")
    @Timed
    public ResponseEntity<OrganizationDTO> updateOrganization(
            @Valid @RequestBody OrganizationDTO organizationDto
    ) throws URISyntaxException, NotAuthorizedException {
        log.debug("REST request to update Organization : {}", organizationDto);
        if (organizationDto.getId() == null) {
            return createOrganization(organizationDto);
        }
        var name = organizationDto.getName();
        checkPermissionOnOrganization(token, ORGANIZATION_UPDATE, name);
        var result = organizationService.save(organizationDto);
        return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getName()))
                .body(result);
    }

    /**
     * GET  /organizations/:organizationName : get the organization with this name.
     *
     * @param name the name of the organizationDTO to retrieve
     * @return the ResponseEntity with status 200 (OK)
     *      and with body the organizationDTO,
     *      or with status 404 (Not Found)
     */
    @GetMapping("/organizations/{name:" + Constants.ENTITY_ID_REGEX + "}")
    @Timed
    public ResponseEntity<OrganizationDTO> getOrganization(
            @PathVariable String name) throws NotAuthorizedException {
        log.debug("REST request to get Organization : {}", name);
        checkPermissionOnOrganization(token, ORGANIZATION_READ, name);
        var org = organizationService.findByName(name);
        var dto = org.orElseThrow(() -> new NotFoundException(
                "Organization not found with name " + name,
                ORGANIZATION, ErrorConstants.ERR_ORGANIZATION_NAME_NOT_FOUND,
                Collections.singletonMap("name", name)));
        return ResponseEntity.ok(dto);
    }

    /**
     * GET  /organizations/:organizationName/projects
     *      : get projects belonging to the organization with this name.
     *
     * @param name the name of the organization
     * @return the ResponseEntity
     *      with status 200 (OK) and with body containing the project list,
     *      or with status 404 (Not Found)
     */
    @GetMapping("/organizations/{name:" + Constants.ENTITY_ID_REGEX + "}/projects")
    @Timed
    public ResponseEntity<List<ProjectDTO>> getOrganizationProjects(
            @PathVariable String name) throws NotAuthorizedException {
        log.debug("REST request to get Projects of the Organization : {}", name);
        checkPermissionOnOrganization(token, ORGANIZATION_READ, name);
        var projects = organizationService.findAllProjectsByOrganizationName(name);
        return ResponseEntity.ok(projects);
    }
}
