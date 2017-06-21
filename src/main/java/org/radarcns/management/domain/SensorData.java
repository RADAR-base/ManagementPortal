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

    //Sensor name.
    @NotNull
    @Column(name = "sensor_name", nullable = false)
    private String sensorName;

    //Default data frequency
    @Column(name = "frequency")
    private String frequency;

    //Measurement unit.
    @Column(name = "unit")
    private String unit;

    // Define if the samples are RAW data or instead they the result of some computation
    @Enumerated(EnumType.STRING)
    @Column(name = "data_type")
    private DataType dataType;

    //  the storage
    @Column(name = "data_class")
    private String dataClass;


    @Column(name = "key_schema")
    private String keySchema;

    @Column(name = "value_schema")
    private String valueSchema;

    @Column(name = "topic")
    private String topic;

    @Column(name = "provider")
    private String provider;

    @Column(name = "enabled")
    private boolean enabled = true;

    @ManyToMany(mappedBy = "sensorData" , fetch = FetchType.EAGER)
    @JsonIgnore
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private Set<DeviceType> deviceTypes = new HashSet<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSensorName() {
        return sensorName;
    }

    public SensorData sensorType(String sensorType) {
        this.sensorName = sensorType;
        return this;
    }

    public void setSensorName(String sensorName) {
        this.sensorName = sensorName;
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

    public String getKeySchema() {
        return keySchema;
    }

    public SensorData dataFormat(String dataFormat) {
        this.keySchema = dataFormat;
        return this;
    }

    public void setKeySchema(String keySchema) {
        this.keySchema = keySchema;
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

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String  getDataClass() {
        return dataClass;
    }

    public void setDataClass(String dataClass) {
        this.dataClass = dataClass;
    }

    public String getValueSchema() {
        return valueSchema;
    }

    public void setValueSchema(String valueSchema) {
        this.valueSchema = valueSchema;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
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
            ", sensorName='" + sensorName + '\'' +
            ", frequency='" + frequency + '\'' +
            ", unit='" + unit + '\'' +
            ", dataType=" + dataType +
            ", dataClass='" + dataClass + '\'' +
            ", keySchema='" + keySchema + '\'' +
            ", valueSchema='" + valueSchema + '\'' +
            ", topic='" + topic + '\'' +
            ", provider='" + provider + '\'' +
            ", enabled=" + enabled +
            ", deviceTypes=" + deviceTypes +
            '}';
    }
}
