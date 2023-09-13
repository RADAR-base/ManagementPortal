package org.radarbase.management.service.dto;


import org.radarbase.management.config.ManagementPortalProperties;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * A DTO for the {@link ManagementPortalProperties.SiteSettings} entity.
 */
public class SiteSettingsDto implements Serializable {

    private List<String> hiddenSubjectFields = List.of();

    public List<String> getHiddenSubjectFields() {
        return hiddenSubjectFields;
    }

    public void setHiddenSubjectFields(List<String> hiddenSubjectFields) {
        this.hiddenSubjectFields = hiddenSubjectFields;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SiteSettingsDto that = (SiteSettingsDto) o;
        return Objects.equals(hiddenSubjectFields, that.hiddenSubjectFields);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hiddenSubjectFields);
    }

    @Override
    public String toString() {
        return "SiteSettingsDTO{"
                + "hiddenSubjectProperties="
                + hiddenSubjectFields
                + '}';
    }
}
