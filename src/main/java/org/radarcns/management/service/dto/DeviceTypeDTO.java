package org.radarcns.management.service.dto;


import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import javax.validation.constraints.NotNull;
import org.radarcns.management.domain.enumeration.SourceType;

/**
 * A DTO for the DeviceType entity.
 */
public class DeviceTypeDTO implements Serializable {

    private Long id;

    private String deviceProducer;

    @NotNull
    private String deviceModel;

    @NotNull
    private SourceType sourceType;

    @NotNull
    private Boolean hasDynamicId = false;

    private Set<SensorDataDTO> sensorData = new HashSet<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public String getDeviceProducer() {
        return deviceProducer;
    }

    public void setDeviceProducer(String deviceProducer) {
        this.deviceProducer = deviceProducer;
    }
    public String getDeviceModel() {
        return deviceModel;
    }

    public void setDeviceModel(String deviceModel) {
        this.deviceModel = deviceModel;
    }
    public SourceType getSourceType() {
        return sourceType;
    }

    public void setSourceType(SourceType sourceType) {
        this.sourceType = sourceType;
    }

    public Set<SensorDataDTO> getSensorData() {
        return sensorData;
    }

    public void setSensorData(Set<SensorDataDTO> sensorData) {
        this.sensorData = sensorData;
    }

    public Boolean getHasDynamicId() {
        return hasDynamicId;
    }

    public void setHasDynamicId(Boolean hasDynamicId) {
        this.hasDynamicId = hasDynamicId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DeviceTypeDTO deviceTypeDTO = (DeviceTypeDTO) o;

        if ( ! Objects.equals(id, deviceTypeDTO.id)) { return false; }

        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "DeviceTypeDTO{" +
            "id=" + id +
            ", deviceProducer='" + deviceProducer + "'" +
            ", deviceModel='" + deviceModel + "'" +
            ", sourceType='" + sourceType + "'" +
            ", hasDynamicId='" + hasDynamicId + "'" +
            '}';
    }
}
