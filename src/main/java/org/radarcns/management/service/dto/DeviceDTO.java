package org.radarcns.management.service.dto;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by nivethika on 23-5-17.
 */
public class DeviceDTO {

    private Long id;

    private String devicePhysicalId;

    private String deviceCategory;

    private Boolean activated;

    private Set<PatientDTO> patients = new HashSet<>();

    private DeviceTypeDTO deviceType;

    private ProjectDTO project;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDevicePhysicalId() {
        return devicePhysicalId;
    }

    public void setDevicePhysicalId(String devicePhysicalId) {
        this.devicePhysicalId = devicePhysicalId;
    }

    public String getDeviceCategory() {
        return deviceCategory;
    }

    public void setDeviceCategory(String deviceCategory) {
        this.deviceCategory = deviceCategory;
    }

    public Boolean getActivated() {
        return activated;
    }

    public void setActivated(Boolean activated) {
        this.activated = activated;
    }

    public Set<PatientDTO> getPatients() {
        return patients;
    }

    public void setPatients(Set<PatientDTO> patients) {
        this.patients = patients;
    }

    public DeviceTypeDTO getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(DeviceTypeDTO deviceType) {
        this.deviceType = deviceType;
    }

    public ProjectDTO getProject() {
        return project;
    }

    public void setProject(ProjectDTO project) {
        this.project = project;
    }
}
