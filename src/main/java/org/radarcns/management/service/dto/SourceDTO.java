package org.radarcns.management.service.dto;


import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * A DTO for the Source entity.
 */
public class SourceDTO implements Serializable {

    private Long id;

    private UUID sourceId;

    @NotNull
    private String sourceName;

    private String expectedSourceName;

    private String deviceCategory;

    @NotNull
    private Boolean assigned;

    @NotNull
    private DeviceTypeDTO deviceType;

    private MinimalProjectDetailsDTO project;

    private Map<String, String> attributes;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public UUID getSourceId() {
        return sourceId;
    }

    public void setSourceId(UUID sourceId) {
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

    public DeviceTypeDTO getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(DeviceTypeDTO deviceType) {
        this.deviceType= deviceType;
    }

    public MinimalProjectDetailsDTO getProject() {
        return project;
    }

    public void setProject(MinimalProjectDetailsDTO project) {
        this.project = project;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public String getExpectedSourceName() {
        return expectedSourceName;
    }

    public void setExpectedSourceName(String expectedSourceName) {
        this.expectedSourceName = expectedSourceName;
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

        return Objects.equals(sourceId, sourceDTO.sourceId)
            && Objects.equals(sourceName, sourceDTO.sourceName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceId, sourceName);
    }

    @Override
    public String toString() {
        return "SourceDTO{" +
            "id=" + id +
            ", sourceId='" + sourceId + '\'' +
            ", sourceName='" + sourceName + '\'' +
            ", deviceCategory='" + deviceCategory + '\'' +
            ", assigned=" + assigned +
            ", deviceType=" + deviceType +
            ", project=" + project +
            '}';
    }
}
