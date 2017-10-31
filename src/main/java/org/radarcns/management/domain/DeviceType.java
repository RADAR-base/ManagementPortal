package org.radarcns.management.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.radarcns.management.domain.enumeration.SourceType;

/**
 * A DeviceType.
 */
@Entity
@Table(name = "device_type")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class DeviceType implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator", initialValue = 1000)
    private Long id;

    @Column(name = "device_producer")
    private String deviceProducer;

    @NotNull
    @Column(name = "device_model", nullable = false )
    private String deviceModel;

    @NotNull
    @Column(name = "catalog_version", nullable = false)
    private String catalogVersion;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false)
    private SourceType sourceType;

    @NotNull
    @Column(name = "dynamic_registration" , nullable = false)
    private Boolean canRegisterDynamically = false;

    @ManyToMany(fetch = FetchType.EAGER)
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @JoinTable(name = "device_type_sensor_data",
               joinColumns = @JoinColumn(name="device_types_id", referencedColumnName="id"),
               inverseJoinColumns = @JoinColumn(name="sensor_data_id", referencedColumnName="id"))
    private Set<SensorData> sensorData = new HashSet<>();

    @ManyToMany(mappedBy = "deviceTypes")
    @JsonIgnore
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private Set<Project> projects = new HashSet<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDeviceProducer() {
        return deviceProducer;
    }

    public DeviceType deviceProducer(String deviceProducer) {
        this.deviceProducer = deviceProducer;
        return this;
    }

    public void setDeviceProducer(String deviceProducer) {
        this.deviceProducer = deviceProducer;
    }

    public String getDeviceModel() {
        return deviceModel;
    }

    public DeviceType deviceModel(String deviceModel) {
        this.deviceModel = deviceModel;
        return this;
    }

    public void setDeviceModel(String deviceModel) {
        this.deviceModel = deviceModel;
    }

    public String getCatalogVersion() {
        return catalogVersion;
    }

    public void setCatalogVersion(String catalogVersion) {
        this.catalogVersion = catalogVersion;
    }

    public DeviceType deviceVersion(String deviceVersion) {
        this.catalogVersion = deviceVersion;
        return this;
    }

    public SourceType getSourceType() {
        return sourceType;
    }

    public DeviceType sourceType(SourceType sourceType) {
        this.sourceType = sourceType;
        return this;
    }

    public void setSourceType(SourceType sourceType) {
        this.sourceType = sourceType;
    }

    public Set<SensorData> getSensorData() {
        return sensorData;
    }

    public DeviceType sensorData(Set<SensorData> sensorData) {
        this.sensorData = sensorData;
        return this;
    }

    public DeviceType addSensorData(SensorData sensorData) {
        this.sensorData.add(sensorData);
        sensorData.getDeviceTypes().add(this);
        return this;
    }

    public DeviceType removeSensorData(SensorData sensorData) {
        this.sensorData.remove(sensorData);
        sensorData.getDeviceTypes().remove(this);
        return this;
    }

    public void setSensorData(Set<SensorData> sensorData) {
        this.sensorData = sensorData;
    }

    public Set<Project> getProjects() {
        return projects;
    }

    public DeviceType projects(Set<Project> projects) {
        this.projects = projects;
        return this;
    }

    public DeviceType addProject(Project project) {
        this.projects.add(project);
        project.getDeviceTypes().add(this);
        return this;
    }

    public DeviceType removeProject(Project project) {
        this.projects.remove(project);
        project.getDeviceTypes().remove(this);
        return this;
    }

    public Boolean getCanRegisterDynamically() {
        return canRegisterDynamically;
    }

    public void setCanRegisterDynamically(Boolean canRegisterDynamically) {
        this.canRegisterDynamically = canRegisterDynamically;
    }

    public void setProjects(Set<Project> projects) {
        this.projects = projects;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DeviceType deviceType = (DeviceType) o;
        if (deviceType.id == null || id == null) {
            return false;
        }
        return Objects.equals(id, deviceType.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "DeviceType{" +
            "id=" + id +
            ", deviceProducer='" + deviceProducer + '\'' +
            ", deviceModel='" + deviceModel + '\'' +
            ", catalogVersion='" + catalogVersion + '\'' +
            ", sourceType=" + sourceType +
            ", canRegisterDynamically=" + canRegisterDynamically +
            '}';
    }
}
