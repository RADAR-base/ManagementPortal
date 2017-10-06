package org.radarcns.management.web.rest;

import com.codahale.metrics.annotation.Timed;
import org.radarcns.auth.authorization.AuthoritiesConstants;
import org.radarcns.management.repository.AuthorityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;

import static org.radarcns.auth.authorization.Permission.AUTHORITY_READ;
import static org.radarcns.auth.authorization.RadarAuthorization.checkPermission;
import static org.radarcns.management.security.SecurityUtils.getJWT;

/**
 * REST controller for managing Authority.
 */
@RestController
@RequestMapping("/api")
public class AuthorityResource {

    @Autowired
    private HttpServletRequest servletRequest;

    @Autowired
    private AuthorityRepository authorityRepository;

    private final Logger log = LoggerFactory.getLogger(AuthorityResource.class);

    /**
     * GET  /authorities : get all the authorities.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of authorities in body
     */
    @GetMapping("/authorities")
    @Timed
    public List<String> getAllAuthorities() {
        log.debug("REST request to get all Authorities");
        checkPermission(getJWT(servletRequest), AUTHORITY_READ);
        return Arrays.asList(AuthoritiesConstants.PROJECT_ADMIN, AuthoritiesConstants.PROJECT_OWNER,
                AuthoritiesConstants.PROJECT_AFFILIATE, AuthoritiesConstants.PROJECT_ANALYST);
    }

}
