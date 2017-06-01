package org.radarcns.management.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.Entity;
import javax.persistence.Table;
import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

import org.hibernate.annotations.Cache;
import org.radarcns.management.domain.enumeration.ProjectStatus;

/**
 * A Project.
 */
@Entity
@Table(name = "project")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Project implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "project_name", nullable = false)
    private String projectName;

    @NotNull
    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "jhi_organization")
    private String organization;

    @NotNull
    @Column(name = "location", nullable = false)
    private String location;

    @Column(name = "start_date")
    private ZonedDateTime startDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "project_status")
    private ProjectStatus projectStatus;

    @Column(name = "end_date")
    private ZonedDateTime endDate;

    @Column(name = "project_admin")
    private Long projectAdmin;

    @JsonIgnore
    @OneToMany(mappedBy = "project")
    @Cascade(org.hibernate.annotations.CascadeType.ALL)
    private Set<Role> roles;

    @ManyToMany
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @JoinTable(name = "project_device_type",
               joinColumns = @JoinColumn(name="projects_id", referencedColumnName="id"),
               inverseJoinColumns = @JoinColumn(name="device_types_id", referencedColumnName="id"))
    private Set<DeviceType> deviceTypes = new HashSet<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProjectName() {
        return projectName;
    }

    public Project projectName(String projectName) {
        this.projectName = projectName;
        return this;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getDescription() {
        return description;
    }

    public Project description(String description) {
        this.description = description;
        return this;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOrganization() {
        return organization;
    }

    public Project organization(String organization) {
        this.organization = organization;
        return this;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public String getLocation() {
        return location;
    }

    public Project location(String location) {
        this.location = location;
        return this;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public ZonedDateTime getStartDate() {
        return startDate;
    }

    public Project startDate(ZonedDateTime startDate) {
        this.startDate = startDate;
        return this;
    }

    public void setStartDate(ZonedDateTime startDate) {
        this.startDate = startDate;
    }

    public ProjectStatus getProjectStatus() {
        return projectStatus;
    }

    public Project projectStatus(ProjectStatus projectStatus) {
        this.projectStatus = projectStatus;
        return this;
    }

    public void setProjectStatus(ProjectStatus projectStatus) {
        this.projectStatus = projectStatus;
    }

    public ZonedDateTime getEndDate() {
        return endDate;
    }

    public Project endDate(ZonedDateTime endDate) {
        this.endDate = endDate;
        return this;
    }

    public void setEndDate(ZonedDateTime endDate) {
        this.endDate = endDate;
    }

    public Long getProjectAdmin() {
        return projectAdmin;
    }

    public Project projectAdmin(Long projectAdmin) {
        this.projectAdmin = projectAdmin;
        return this;
    }

    public void setProjectAdmin(Long projectAdmin) {
        this.projectAdmin = projectAdmin;
    }

    public Set<DeviceType> getDeviceTypes() {
        return deviceTypes;
    }

    public Project deviceTypes(Set<DeviceType> deviceTypes) {
        this.deviceTypes = deviceTypes;
        return this;
    }

    public Project addDeviceType(DeviceType deviceType) {
        this.deviceTypes.add(deviceType);
        deviceType.getProjects().add(this);
        return this;
    }

    public Project removeDeviceType(DeviceType deviceType) {
        this.deviceTypes.remove(deviceType);
        deviceType.getProjects().remove(this);
        return this;
    }

    public void setDeviceTypes(Set<DeviceType> deviceTypes) {
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
        Project project = (Project) o;
        if (project.id == null || id == null) {
            return false;
        }
        return Objects.equals(id, project.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Project{" +
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
