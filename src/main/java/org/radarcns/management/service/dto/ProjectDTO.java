package org.radarcns.management.service.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.validation.constraints.NotNull;
import org.radarcns.management.domain.enumeration.ProjectStatus;

/**
 * A DTO for the Project entity.
 */
public class ProjectDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String EXTERNAL_PROJECT_URL_KEY = "External-project-url";
    public static final String EXTERNAL_PROJECT_ID_KEY = "External-project-id";
    public static final String WORK_PACKAGE_KEY = "Work-package";
    public static final String PHASE_KEY = "Phase";
    public static final String HUMAN_READABLE_PROJECT_NAME = "Human-readable-project-name";
    public static final String PRIVACY_POLICY_URL = "Privacy-policy-url";

    private Long id;

    @NotNull
    private String projectName;

    private String humanReadableProjectName;

    @NotNull
    private String description;

    private String organization;

    @NotNull
    private String location;

    private ZonedDateTime startDate;

    private ProjectStatus projectStatus;

    private ZonedDateTime endDate;

    @JsonInclude(Include.NON_NULL)
    private Set<SourceTypeDTO> sourceTypes;

    private Map<String, String> attributes;

    private Long persistentTokenTimeout;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public ZonedDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(ZonedDateTime startDate) {
        this.startDate = startDate;
    }

    public ProjectStatus getProjectStatus() {
        return projectStatus;
    }

    public void setProjectStatus(ProjectStatus projectStatus) {
        this.projectStatus = projectStatus;
    }

    public ZonedDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(ZonedDateTime endDate) {
        this.endDate = endDate;
    }

    public Set<SourceTypeDTO> getSourceTypes() {
        return sourceTypes;
    }

    public void setSourceTypes(Set<SourceTypeDTO> sourceTypes) {
        this.sourceTypes = sourceTypes;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    public String getHumanReadableProjectName() {
        return humanReadableProjectName;
    }

    public void setHumanReadableProjectName(String humanReadableProjectName) {
        this.humanReadableProjectName = humanReadableProjectName;
    }

    public Long getPersistentTokenTimeout() {
        return persistentTokenTimeout;
    }

    public void setPersistentTokenTimeout(Long persistentTokenTimeout) {
        this.persistentTokenTimeout = persistentTokenTimeout;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ProjectDTO projectDto = (ProjectDTO) o;
        if (id == null || projectDto.id == null) {
            return false;
        }

        return Objects.equals(id, projectDto.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "ProjectDTO{"
                + "id=" + id
                + ", projectName='" + projectName + "'"
                + ", description='" + description + "'"
                + ", organization='" + organization + "'"
                + ", location='" + location + "'"
                + ", startDate='" + startDate + "'"
                + ", projectStatus='" + projectStatus + "'"
                + ", endDate='" + endDate + "'"
                + '}';
    }
}
