package org.radarcns.management.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "device_producer")
    private String deviceProducer;

    @NotNull
    @Column(name = "device_model", nullable = false)
    private String deviceModel;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false)
    private SourceType sourceType;

    @ManyToMany
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @JoinTable(name = "device_type_sensor_data",
               joinColumns = @JoinColumn(name="device_types_id", referencedColumnName="id"),
               inverseJoinColumns = @JoinColumn(name="sensor_data_id", referencedColumnName="id"))
    private Set<SensorData> sensorData = new HashSet<>();

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
            ", deviceProducer='" + deviceProducer + "'" +
            ", deviceModel='" + deviceModel + "'" +
            ", sourceType='" + sourceType + "'" +
            '}';
    }
}
