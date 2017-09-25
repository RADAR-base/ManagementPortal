package org.radarcns.management.service.dto;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Created by nivethika on 21-9-17.
 */
public class SourceRegistrationDTO {

    private UUID sourceId;

    private String expectedSourceName ;

    private String sourceName;

    private String deviceTypeModel;

    private String deviceTypeProducer;

    private String deviceCatalogVersion;

    private Long projectId;

    private boolean assigned;

    private Set<AttributeMapDTO> metaData = new HashSet<>();

    public UUID getSourceId() {
        return sourceId;
    }

    public void setSourceId(UUID sourceId) {
        this.sourceId = sourceId;
    }

    public String getDeviceTypeModel() {
        return deviceTypeModel;
    }

    public void setDeviceTypeModel(String deviceTypeModel) {
        this.deviceTypeModel = deviceTypeModel;
    }

    public String getDeviceTypeProducer() {
        return deviceTypeProducer;
    }

    public void setDeviceTypeProducer(String deviceTypeProducer) {
        this.deviceTypeProducer = deviceTypeProducer;
    }

    public String getDeviceCatalogVersion() {
        return deviceCatalogVersion;
    }

    public void setDeviceCatalogVersion(String deviceCatalogVersion) {
        this.deviceCatalogVersion = deviceCatalogVersion;
    }

    public String getExpectedSourceName() {
        return expectedSourceName;
    }

    public void setExpectedSourceName(String expectedSourceName) {
        this.expectedSourceName = expectedSourceName;
    }

    public Set<AttributeMapDTO> getMetaData() {
        return metaData;
    }

    public void setMetaData(Set<AttributeMapDTO> metaData) {
        this.metaData = metaData;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public boolean isAssigned() {
        return assigned;
    }

    public void setAssigned(boolean assigned) {
        this.assigned = assigned;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }
}
