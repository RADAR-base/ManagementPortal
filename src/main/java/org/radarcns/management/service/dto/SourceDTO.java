package org.radarcns.management.service.dto;


import javax.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the Source entity.
 */
public class SourceDTO implements Serializable {

    private Long id;

    @NotNull
    private String sourceId;

    private String deviceCategory;

    @NotNull
    private Boolean assigned;

    private Long deviceTypeId;

    private Long projectId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }
    public String getDeviceCategory() {
        return deviceCategory;
    }

    public void setDeviceCategory(String deviceCategory) {
        this.deviceCategory = deviceCategory;
    }
    public Boolean getAssigned() {
        return assigned;
    }

    public void setAssigned(Boolean assigned) {
        this.assigned = assigned;
    }

    public Long getDeviceTypeId() {
        return deviceTypeId;
    }

    public void setDeviceTypeId(Long deviceTypeId) {
        this.deviceTypeId = deviceTypeId;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SourceDTO sourceDTO = (SourceDTO) o;

        if ( ! Objects.equals(id, sourceDTO.id)) { return false; }

        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "SourceDTO{" +
            "id=" + id +
            ", sourceId='" + sourceId + "'" +
            ", deviceCategory='" + deviceCategory + "'" +
            ", activated='" + assigned + "'" +
            '}';
    }
}
