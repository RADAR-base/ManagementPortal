package org.radarcns.management.service.dto;

import java.util.HashSet;
import java.util.Set;
import org.radarcns.management.domain.enumeration.SourceType;

/**
 * Created by nivethika on 23-5-17.
 */
public class DeviceTypeDTO {

    private Long id;

    private String deviceProducer;

    private String deviceModel;

    private SourceType sourceType;

    private Set<SensorDataDTO> sensorData = new HashSet<>();

    private Set<ProjectDTO> projects = new HashSet<>();

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

    public Set<ProjectDTO> getProjects() {
        return projects;
    }

    public void setProjects(Set<ProjectDTO> projects) {
        this.projects = projects;
    }
}
