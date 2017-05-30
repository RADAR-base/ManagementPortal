package org.radarcns.management.web.rest;

import com.codahale.metrics.annotation.Timed;
import java.util.List;
import java.util.stream.Collectors;
import org.radarcns.management.repository.AuthorityRepository;
import org.radarcns.management.repository.RoleRepository;
import org.radarcns.management.security.AuthoritiesConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * REST controller for managing Role.
 */
@RestController
@RequestMapping("/api")
public class RoleResource {


    private final Logger log = LoggerFactory.getLogger(DeviceTypeResource.class);

    private final RoleRepository roleRepository;

    public RoleResource(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    /**
     * GET  /device-types : get all the authorities.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of authorities in body
     */
    @GetMapping("/roles")
    @Timed
    @Secured({ AuthoritiesConstants.SYS_ADMIN, AuthoritiesConstants.PROJECT_ADMIN})
    public List<String> getAllAuthorities() {
        log.debug("REST request to get all Authorities");
        List<String> authorities = roleRepository.findAll().stream().map( role -> role.getAuthority().getName()).collect(
            Collectors.toList());
        return authorities;
    }

}
