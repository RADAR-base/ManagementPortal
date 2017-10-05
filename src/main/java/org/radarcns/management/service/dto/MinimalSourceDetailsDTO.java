package org.radarcns.management.service.dto;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by nivethika on 13-6-17.
 */
public class MinimalSourceDetailsDTO {
    private Long id;
    private Long deviceTypeId;
    private String deviceTypeProducer;
    private String deviceTypeModel;
    private String deviceTypeCatalogVersion;
    private String expectedSourceName;
    private UUID sourceId;
    private String sourceName;
    private boolean assigned;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    private Map<String, String> attributes = new HashMap<>();

    public Long getDeviceTypeId() {
        return deviceTypeId;
    }

    public void setDeviceTypeId(Long deviceTypeId) {
        this.deviceTypeId = deviceTypeId;
    }

    public String getExpectedSourceName() {
        return expectedSourceName;
    }

    public void setExpectedSourceName(String expectedSourceName) {
        this.expectedSourceName = expectedSourceName;
    }

    public UUID getSourceId() {
        return sourceId;
    }

    public void setSourceId(UUID sourceId) {
        this.sourceId = sourceId;
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

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    public String getDeviceTypeCatalogVersion() {
        return deviceTypeCatalogVersion;
    }

    public void setDeviceTypeCatalogVersion(String deviceTypeCatalogVersion) {
        this.deviceTypeCatalogVersion = deviceTypeCatalogVersion;
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
}
