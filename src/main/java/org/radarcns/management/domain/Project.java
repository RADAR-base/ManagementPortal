package org.radarcns.management.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

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

    @Column(name = "project_owner")
    private Long projectOwner;

    @ManyToMany
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @JoinTable(name = "project_device",
               joinColumns = @JoinColumn(name="projects_id", referencedColumnName="id"),
               inverseJoinColumns = @JoinColumn(name="devices_id", referencedColumnName="id"))
    private Set<Device> devices = new HashSet<>();

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

    public Long getProjectOwner() {
        return projectOwner;
    }

    public Project projectOwner(Long projectOwner) {
        this.projectOwner = projectOwner;
        return this;
    }

    public void setProjectOwner(Long projectOwner) {
        this.projectOwner = projectOwner;
    }

    public Set<Device> getDevices() {
        return devices;
    }

    public Project devices(Set<Device> devices) {
        this.devices = devices;
        return this;
    }

    public Project addDevice(Device device) {
        this.devices.add(device);
        device.getProjects().add(this);
        return this;
    }

    public Project removeDevice(Device device) {
        this.devices.remove(device);
        device.getProjects().remove(this);
        return this;
    }

    public void setDevices(Set<Device> devices) {
        this.devices = devices;
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
            ", projectOwner='" + projectOwner + "'" +
            '}';
    }
}
