package org.radarcns.management.service.dto;


import java.time.ZonedDateTime;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;
import org.radarcns.management.domain.enumeration.ProjectStatus;

/**
 * A DTO for the Project entity.
 */
public class ProjectDTO implements Serializable {

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

    private Set<DeviceTypeDTO> deviceTypes = new HashSet<>();

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

    public Set<DeviceTypeDTO> getDeviceTypes() {
        return deviceTypes;
    }

    public void setDeviceTypes(Set<DeviceTypeDTO> deviceTypes) {
        this.deviceTypes = deviceTypes;
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

        if ( ! Objects.equals(id, projectDTO.id)) { return false; }

        return true;
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
