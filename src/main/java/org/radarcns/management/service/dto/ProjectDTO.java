package org.radarcns.management.service.dto;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;
import org.radarcns.management.domain.Project;
import org.radarcns.management.domain.enumeration.ProjectStatus;

/**
 * Created by nivethika on 23-5-17.
 */
public class ProjectDTO {

    private Long id;

    private String projectName;

    private String description;

    private String organization;

    private String location;

    private ZonedDateTime startDate;

    private ProjectStatus projectStatus;

    private ZonedDateTime endDate;

    private Long projectAdmin;

    private Set<RoleDTO> roles;

    private Set<DeviceTypeDTO> deviceTypes = new HashSet<>();

    public ProjectDTO () {

    }

    public ProjectDTO(Project project) {

    }

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

    public Set<RoleDTO> getRoles() {
        return roles;
    }

    public void setRoles(Set<RoleDTO> roles) {
        this.roles = roles;
    }

    public Set<DeviceTypeDTO> getDeviceTypes() {
        return deviceTypes;
    }

    public void setDeviceTypes(Set<DeviceTypeDTO> deviceTypes) {
        this.deviceTypes = deviceTypes;
    }
}
