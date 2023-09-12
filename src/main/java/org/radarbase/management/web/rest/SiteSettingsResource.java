package org.radarbase.management.web.rest;

import io.micrometer.core.annotation.Timed;
import org.radarbase.management.service.SiteSettingsService;
import org.radarbase.management.service.dto.SiteSettingsDTO;
import org.radarbase.management.config.ManagementPortalProperties.SiteSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for managing site settings.
 *
 * <p>This class accesses the {@link SiteSettings} entity as a means of defining configurations before authentication.</p>
 *
 */
@RestController
@RequestMapping("/api")
public class SiteSettingsResource {

    private static final Logger log = LoggerFactory.getLogger(SiteSettingsResource.class);

    private final SiteSettingsService siteSettingsService;

    public SiteSettingsResource(
            @Autowired SiteSettingsService siteSettingsService) {
        this.siteSettingsService = siteSettingsService;
    }

    /**
     * GET  /SiteSettings  : Gets the current SiteSettings as a DTO
     *
     * @return the ResponseEntity with status 200 (Ok) and with body {@link SiteSettingsDTO}.
     */
    @GetMapping("/sitesettings")
    @Timed
    public ResponseEntity<SiteSettingsDTO> getDisabledSubjectFields() {
        log.debug("REST request to get sitesettings");

        return ResponseEntity
                .ok()
                .body(siteSettingsService.getSiteSettingsDTO());
    }
}
