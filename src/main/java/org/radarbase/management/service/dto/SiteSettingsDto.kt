package org.radarbase.management.service.dto

import org.radarbase.management.config.ManagementPortalProperties
import java.io.Serializable
import java.util.*

/**
 * A DTO for the [ManagementPortalProperties.SiteSettings] entity.
 */
class SiteSettingsDto : Serializable {
    var hiddenSubjectFields = listOf<String>()
    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || javaClass != other.javaClass) {
            return false
        }
        val that = other as SiteSettingsDto
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
