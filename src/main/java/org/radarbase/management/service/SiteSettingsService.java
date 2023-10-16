package org.radarbase.management.service;

import org.radarbase.management.config.ManagementPortalProperties.SiteSettings;
import org.radarbase.management.config.ManagementPortalProperties;
import org.radarbase.management.service.dto.SiteSettingsDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service class for managing SiteSettings.
 */
@Service
@Transactional
public class SiteSettingsService {

    @Autowired
    private ManagementPortalProperties managementPortalProperties;

    /**
     * Convert a {@link SiteSettings} to a {@link SiteSettingsDto} object.
     * @param siteSettings The object to convert
     * @return the newly created DTO object
     */
    public SiteSettingsDto createSiteSettingsDto(SiteSettings siteSettings) {

        SiteSettingsDto siteSettingsDto = new SiteSettingsDto();

        siteSettingsDto.setHiddenSubjectFields(siteSettings.getHiddenSubjectFields());
        return siteSettingsDto;
    }

    // NAMING!
    public SiteSettingsDto getSiteSettingsDto() {
        return createSiteSettingsDto(managementPortalProperties.getSiteSettings());
    }
}
