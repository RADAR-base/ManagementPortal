package org.radarbase.management.web.rest

import io.micrometer.core.annotation.Timed
import org.radarbase.management.service.SiteSettingsService
import org.radarbase.management.service.dto.SiteSettingsDto
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * REST controller for managing site settings.
 *
 *
 * This class accesses [SiteSettings] entity as a means of defining configurations
 * before authentication.
 *
 */
@RestController
@RequestMapping("/api")
class SiteSettingsResource(
    @param:Autowired private val siteSettingsService: SiteSettingsService
) {
    @get:Timed
    @get:GetMapping("/sitesettings")
    val disabledSubjectFields: ResponseEntity<SiteSettingsDto>
        /**
         * GET  /SiteSettings  : Gets the current SiteSettings as a DTO.
         *
         * @return the ResponseEntity with status 200 (Ok) and with body [SiteSettingsDto].
         */
        get() {
            log.debug("REST request to get sitesettings")
            return ResponseEntity
                .ok()
                .body(siteSettingsService.siteSettingsDto)
        }

    companion object {
        private val log = LoggerFactory.getLogger(SiteSettingsResource::class.java)
    }
}
