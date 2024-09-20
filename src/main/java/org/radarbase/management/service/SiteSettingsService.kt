package org.radarbase.management.service

import org.radarbase.management.config.ManagementPortalProperties
import org.radarbase.management.config.ManagementPortalProperties.SiteSettings
import org.radarbase.management.service.dto.SiteSettingsDto
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Service class for managing SiteSettings.
 */
@Service
@Transactional
class SiteSettingsService(
    @Autowired private val managementPortalProperties: ManagementPortalProperties,
) {
    /**
     * Convert a [SiteSettings] to a [SiteSettingsDto] object.
     * @param siteSettings The object to convert
     * @return the newly created DTO object
     */
    fun createSiteSettingsDto(siteSettings: SiteSettings): SiteSettingsDto {
        val siteSettingsDto = SiteSettingsDto()
        siteSettingsDto.hiddenSubjectFields = siteSettings.hiddenSubjectFields ?: emptyList()
        return siteSettingsDto
    }

    val siteSettingsDto: SiteSettingsDto
        get() = createSiteSettingsDto(managementPortalProperties.siteSettings)
}
