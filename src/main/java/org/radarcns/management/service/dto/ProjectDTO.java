package org.radarcns.management.service.dto;


import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.HashSet;
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

    private Long id;

    @NotNull
    private String projectName;

    @NotNull
    private String description;

    private String organization;

    @NotNull
    private String location;

    private ZonedDateTime startDate;

    private ProjectStatus projectStatus;

    private ZonedDateTime endDate;

    private Long projectAdmin;

    private Set<SourceTypeDTO> sourceTypes = new HashSet<>();

    private Set<AttributeMapDTO> attributes = new HashSet<>();

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
    public Long getProjectAdmin() {
        return projectAdmin;
    }

    public void setProjectAdmin(Long projectAdmin) {
        this.projectAdmin = projectAdmin;
    }

    public Set<SourceTypeDTO> getSourceTypes() {
        return sourceTypes;
    }

    public void setSourceTypes(Set<SourceTypeDTO> sourceTypes) {
        this.sourceTypes = sourceTypes;
    }

    public Set<AttributeMapDTO> getAttributes() {
        return attributes;
    }

    public void setAttributes(Set<AttributeMapDTO> attributes) {
        this.attributes = attributes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ProjectDTO projectDTO = (ProjectDTO) o;
        if (id ==null || projectDTO.id ==null) {
            return  false;
        }

        return Objects.equals(id, projectDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "ProjectDTO{" +
            "id=" + id +
            ", projectName='" + projectName + "'" +
            ", description='" + description + "'" +
            ", organization='" + organization + "'" +
            ", location='" + location + "'" +
            ", startDate='" + startDate + "'" +
            ", projectStatus='" + projectStatus + "'" +
            ", endDate='" + endDate + "'" +
            ", projectAdmin='" + projectAdmin + "'" +
            '}';
    }
}
