package org.radarcns.management.service.dto;

import java.util.HashSet;
import java.util.Set;
import org.radarcns.management.domain.enumeration.DataType;

/**
 * Created by nivethika on 23-5-17.
 */
public class SensorDataDTO {

    private Long id;

    private String sensorType;

    private DataType dataType;

    private String dataFormat;

    private String frequency;

    private Set<DeviceTypeDTO> deviceTypes = new HashSet<>();

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

    public Set<DeviceTypeDTO> getDeviceTypes() {
        return deviceTypes;
    }

    public void setDeviceTypes(Set<DeviceTypeDTO> deviceTypes) {
        this.deviceTypes = deviceTypes;
    }
}
