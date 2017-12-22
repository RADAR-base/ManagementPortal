package org.radarcns.management.service.dto;


import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import javax.validation.constraints.NotNull;

/**
 * A DTO for the Source entity.
 */
public class SourceDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private UUID sourceId;

    @NotNull
    private String sourceName;

    private String expectedSourceName;

    @NotNull
    private Boolean assigned;

    @NotNull
    private SourceTypeDTO sourceType;

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

    public Boolean getAssigned() {
        return assigned;
    }

    public void setAssigned(Boolean assigned) {
        this.assigned = assigned;
    }

    public SourceTypeDTO getSourceType() {
        return sourceType;
    }

    public void setSourceType(SourceTypeDTO sourceType) {
        this.sourceType = sourceType;
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
                ", assigned=" + assigned +
                ", sourceType=" + sourceType +
                ", project=" + project +
                '}';
    }
}
