package org.radarbase.management.web.rest;

import static org.radarbase.auth.authorization.Permission.AUTHORITY_READ;
import static org.radarbase.auth.authorization.RadarAuthorization.checkPermission;

import io.micrometer.core.annotation.Timed;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.radarbase.auth.authorization.RoleAuthority;
import org.radarbase.auth.exception.NotAuthorizedException;
import org.radarbase.auth.token.RadarToken;
import org.radarbase.management.service.dto.AuthorityDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for managing Authority.
 */
@RestController
@RequestMapping("/api")
public class AuthorityResource {
    @Autowired
    private RadarToken token;

    private static final Logger log = LoggerFactory.getLogger(AuthorityResource.class);

    /**
     * GET  /authorities : get all the authorities.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of authorities in body
     */
    @GetMapping("/authorities")
    @Timed
    public List<AuthorityDTO> getAllAuthorities() throws NotAuthorizedException {
        log.debug("REST request to get all Authorities");
        checkPermission(token, AUTHORITY_READ);
        return Stream.of(
                RoleAuthority.SYS_ADMIN,
                RoleAuthority.ORGANIZATION_ADMIN,
                RoleAuthority.PROJECT_ADMIN,
                RoleAuthority.PROJECT_OWNER,
                RoleAuthority.PROJECT_AFFILIATE,
                RoleAuthority.PROJECT_ANALYST)
                .map(AuthorityDTO::new)
                .collect(Collectors.toList());
    }
}
