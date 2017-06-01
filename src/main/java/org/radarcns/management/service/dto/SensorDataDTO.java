package org.radarcns.management.service.dto;


import javax.validation.constraints.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;
import org.radarcns.management.domain.enumeration.DataType;

/**
 * A DTO for the SensorData entity.
 */
public class SensorDataDTO implements Serializable {

    private Long id;

    @NotNull
    private String sensorType;

    private DataType dataType;

    private String dataFormat;

    private String frequency;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public String getSensorType() {
        return sensorType;
    }

    public void setSensorType(String sensorType) {
        this.sensorType = sensorType;
    }
    public DataType getDataType() {
        return dataType;
    }

    public void setDataType(DataType dataType) {
        this.dataType = dataType;
    }
    public String getDataFormat() {
        return dataFormat;
    }

    public void setDataFormat(String dataFormat) {
        this.dataFormat = dataFormat;
    }
    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SensorDataDTO sensorDataDTO = (SensorDataDTO) o;

        if ( ! Objects.equals(id, sensorDataDTO.id)) { return false; }

        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "SensorDataDTO{" +
            "id=" + id +
            ", sensorType='" + sensorType + "'" +
            ", dataType='" + dataType + "'" +
            ", dataFormat='" + dataFormat + "'" +
            ", frequency='" + frequency + "'" +
            '}';
    }
}
