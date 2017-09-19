package org.radarcns.management.web.rest;

import static org.radarcns.management.security.AuthoritiesConstants.PROJECT_RELATED_AUTHORITIES;

import com.codahale.metrics.annotation.Timed;
import java.util.Arrays;
import java.util.List;
import org.radarcns.management.repository.AuthorityRepository;
import org.radarcns.management.security.AuthoritiesConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for managing Authority.
 */
@RestController
@RequestMapping("/api")
public class AuthorityResource {


    private final Logger log = LoggerFactory.getLogger(DeviceTypeResource.class);

    private final AuthorityRepository authorityRepository;

    public AuthorityResource(AuthorityRepository authorityRepository) {
        this.authorityRepository = authorityRepository;
    }

    /**
     * GET  /device-types : get all the authorities.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of authorities in body
     */
    @GetMapping("/authorities")
    @Timed
    @Secured({ AuthoritiesConstants.SYS_ADMIN, AuthoritiesConstants.PROJECT_ADMIN})
    public List<String> getAllAuthorities() {
        log.debug("REST request to get all Authorities");
        return Arrays.asList(PROJECT_RELATED_AUTHORITIES);
    }

}
