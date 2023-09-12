package org.radarbase.management.service.dto;


import org.radarbase.management.config.ManagementPortalProperties;

import java.io.Serializable;
import java.util.*;

/**
 * A DTO for the {@link ManagementPortalProperties.SiteSettings} entity.
 */
public class SiteSettingsDTO implements Serializable {

    private List<String> hiddenSubjectFields = List.of();

    public List<String> getHiddenSubjectFields() {
        return hiddenSubjectFields;
    }

    public void setHiddenSubjectFields(List<String> hiddenSubjectFields) {
        this.hiddenSubjectFields = hiddenSubjectFields;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SiteSettingsDTO that = (SiteSettingsDTO) o;
        return Objects.equals(hiddenSubjectFields, that.hiddenSubjectFields);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hiddenSubjectFields);
    }

    @Override
    public String toString() {
        return "SiteSettingsDTO{" +
                "hiddenSubjectProperties=" + hiddenSubjectFields +
                '}';
    }
}
