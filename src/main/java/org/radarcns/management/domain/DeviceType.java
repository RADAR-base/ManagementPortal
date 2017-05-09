package org.radarcns.management.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;

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
    @Column(name = "sensor_type", nullable = false)
    private String sensorType;

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

    public String getSensorType() {
        return sensorType;
    }

    public DeviceType sensorType(String sensorType) {
        this.sensorType = sensorType;
        return this;
    }

    public void setSensorType(String sensorType) {
        this.sensorType = sensorType;
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
            ", sensorType='" + sensorType + "'" +
            '}';
    }
}
