package org.radarbase.management.service;

import org.radarbase.management.config.ManagementPortalProperties.SiteSettings;
import org.radarbase.management.config.ManagementPortalProperties;
import org.radarbase.management.service.dto.SiteSettingsDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service class for managing SiteSettings.
 */
@Service
@Transactional
public class SiteSettingsService {

    private static final Logger log = LoggerFactory.getLogger(SiteSettingsService.class);

    @Autowired
    private ManagementPortalProperties managementPortalProperties;

    /**
     * Convert a {@link SiteSettings} to a {@link SiteSettingsDTO} object
     * @param siteSettings The object to convert
     * @return the newly created DTO object
     */
    public SiteSettingsDTO createSiteSettingsDTO(SiteSettings siteSettings) {
        SiteSettingsDTO siteSettingsDTO = new SiteSettingsDTO();
        siteSettingsDTO.setHiddenSubjectFields(siteSettings.getHiddenSubjectFields());
        return siteSettingsDTO;
    }

    /**
     * Convert a {@link SiteSettingsDTO} to a {@link SiteSettings} object.
     *
     * @param siteSettingsDTO The DTO object to convert
     * @return the newly created object
     */
    public SiteSettings createSiteSettings(SiteSettingsDTO siteSettingsDTO) {
        SiteSettings siteSettings = new SiteSettings();
        siteSettings.setHiddenSubjectFields(siteSettingsDTO.getHiddenSubjectFields());
        return siteSettings;
    }

    // NAMING!
    public SiteSettingsDTO getSiteSettingsDTO(){
        return createSiteSettingsDTO(managementPortalProperties.getSiteSettings());
    }
}
