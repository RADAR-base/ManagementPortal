package org.radarbase.management.service.dto

import org.radarbase.management.config.ManagementPortalProperties
import java.io.Serializable
import java.util.*

/**
 * A DTO for the [ManagementPortalProperties.SiteSettings] entity.
 */
class SiteSettingsDto : Serializable {
    var hiddenSubjectFields = listOf<String>()
    override fun equals(o: Any?): Boolean {
        if (this === o) {
            return true
        }
        if (o == null || javaClass != o.javaClass) {
            return false
        }
        val that = o as SiteSettingsDto
        return hiddenSubjectFields == that.hiddenSubjectFields
    }

    override fun hashCode(): Int {
        return Objects.hash(hiddenSubjectFields)
    }

    override fun toString(): String {
        return ("SiteSettingsDTO{"
                + "hiddenSubjectProperties="
                + hiddenSubjectFields
                + '}')
    }
}
