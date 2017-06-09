package org.radarcns.management.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

import org.radarcns.management.domain.enumeration.DataType;

/**
 * A SensorData.
 */
@Entity
@Table(name = "sensor_data")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class SensorData implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "sensor_type", nullable = false)
    private String sensorType;

    @Enumerated(EnumType.STRING)
    @Column(name = "data_type")
    private DataType dataType;

    @Column(name = "data_format")
    private String dataFormat;

    @Column(name = "frequency")
    private String frequency;

    @ManyToMany(mappedBy = "sensorData")
    @JsonIgnore
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private Set<DeviceType> deviceTypes = new HashSet<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSensorType() {
        return sensorType;
    }

    public SensorData sensorType(String sensorType) {
        this.sensorType = sensorType;
        return this;
    }

    public void setSensorType(String sensorType) {
        this.sensorType = sensorType;
    }

    public DataType getDataType() {
        return dataType;
    }

    public SensorData dataType(DataType dataType) {
        this.dataType = dataType;
        return this;
    }

    public void setDataType(DataType dataType) {
        this.dataType = dataType;
    }

    public String getDataFormat() {
        return dataFormat;
    }

    public SensorData dataFormat(String dataFormat) {
        this.dataFormat = dataFormat;
        return this;
    }

    public void setDataFormat(String dataFormat) {
        this.dataFormat = dataFormat;
    }

    public String getFrequency() {
        return frequency;
    }

    public SensorData frequency(String frequency) {
        this.frequency = frequency;
        return this;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public Set<DeviceType> getDeviceTypes() {
        return deviceTypes;
    }

    public SensorData deviceTypes(Set<DeviceType> deviceTypes) {
        this.deviceTypes = deviceTypes;
        return this;
    }

    public SensorData addDeviceType(DeviceType deviceType) {
        this.deviceTypes.add(deviceType);
        deviceType.getSensorData().add(this);
        return this;
    }

    public SensorData removeDeviceType(DeviceType deviceType) {
        this.deviceTypes.remove(deviceType);
        deviceType.getSensorData().remove(this);
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
        SensorData sensorData = (SensorData) o;
        if (sensorData.id == null || id == null) {
            return false;
        }
        return Objects.equals(id, sensorData.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "SensorData{" +
            "id=" + id +
            ", sensorType='" + sensorType + "'" +
            ", dataType='" + dataType + "'" +
            ", dataFormat='" + dataFormat + "'" +
            ", frequency='" + frequency + "'" +
            '}';
    }
}
