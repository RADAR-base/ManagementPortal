package org.radarbase.management.web.rest;

import io.micrometer.core.annotation.Timed;
import org.radarbase.management.config.ManagementPortalProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URISyntaxException;

/**
 * REST controller for managing site properties.
 *
 * <p>This class accesses the SiteProperties entity as a means of defining configurations before authentication.</p>
 *
 */
@RestController
@RequestMapping("/api")
public class SitePropertiesResource {

    private static final Logger log = LoggerFactory.getLogger(SitePropertiesResource.class);

    @Autowired
    private ManagementPortalProperties managementPortalProperties;

    /**
     * GET  /siteproperties/disabledsubjectproperties  : Creates a new user. <p> Creates a new user if the login and email are not
     * already used, and sends an mail with an activation link. The user needs to be activated on
     * creation. </p>
     *
     * @return the ResponseEntity with status 201 (Created) and with body the new user, or with
     *     status 400 (Bad Request) if the login or email is already in use
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @GetMapping("/siteproperties/disabledsubjectproperties")
    @Timed
    public ResponseEntity<String[]> getDisabledSubjectProperties()
            throws URISyntaxException {
        log.debug("REST request to get SiteProperties");

        return ResponseEntity
                .ok()
                .body(managementPortalProperties.getSiteProperties().getDisabledSubjectProperties());
    }
}
