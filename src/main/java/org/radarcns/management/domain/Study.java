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

import org.radarcns.management.domain.enumeration.StudyStatus;

/**
 * A Study.
 */
@Entity
@Table(name = "study")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Study implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "study_name", nullable = false)
    private String studyName;

    @NotNull
    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "start_date")
    private ZonedDateTime startDate;

    @Column(name = "end_date")
    private ZonedDateTime endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "study_status")
    private StudyStatus studyStatus;

    @ManyToOne
    private Project parentProjectId;

    @ManyToMany
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @JoinTable(name = "study_device",
               joinColumns = @JoinColumn(name="studies_id", referencedColumnName="id"),
               inverseJoinColumns = @JoinColumn(name="devices_id", referencedColumnName="id"))
    private Set<Device> devices = new HashSet<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStudyName() {
        return studyName;
    }

    public Study studyName(String studyName) {
        this.studyName = studyName;
        return this;
    }

    public void setStudyName(String studyName) {
        this.studyName = studyName;
    }

    public String getDescription() {
        return description;
    }

    public Study description(String description) {
        this.description = description;
        return this;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ZonedDateTime getStartDate() {
        return startDate;
    }

    public Study startDate(ZonedDateTime startDate) {
        this.startDate = startDate;
        return this;
    }

    public void setStartDate(ZonedDateTime startDate) {
        this.startDate = startDate;
    }

    public ZonedDateTime getEndDate() {
        return endDate;
    }

    public Study endDate(ZonedDateTime endDate) {
        this.endDate = endDate;
        return this;
    }

    public void setEndDate(ZonedDateTime endDate) {
        this.endDate = endDate;
    }

    public StudyStatus getStudyStatus() {
        return studyStatus;
    }

    public Study studyStatus(StudyStatus studyStatus) {
        this.studyStatus = studyStatus;
        return this;
    }

    public void setStudyStatus(StudyStatus studyStatus) {
        this.studyStatus = studyStatus;
    }

    public Project getParentProjectId() {
        return parentProjectId;
    }

    public Study parentProjectId(Project project) {
        this.parentProjectId = project;
        return this;
    }

    public void setParentProjectId(Project project) {
        this.parentProjectId = project;
    }

    public Set<Device> getDevices() {
        return devices;
    }

    public Study devices(Set<Device> devices) {
        this.devices = devices;
        return this;
    }

    public Study addDevice(Device device) {
        this.devices.add(device);
        device.getStudies().add(this);
        return this;
    }

    public Study removeDevice(Device device) {
        this.devices.remove(device);
        device.getStudies().remove(this);
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
        Study study = (Study) o;
        if (study.id == null || id == null) {
            return false;
        }
        return Objects.equals(id, study.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Study{" +
            "id=" + id +
            ", studyName='" + studyName + "'" +
            ", description='" + description + "'" +
            ", startDate='" + startDate + "'" +
            ", endDate='" + endDate + "'" +
            ", studyStatus='" + studyStatus + "'" +
            '}';
    }
}
