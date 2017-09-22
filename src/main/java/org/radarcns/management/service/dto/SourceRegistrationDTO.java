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

    private String deviceTypeModel;

    private String deviceTypeProducer;

    private String deviceTypeVersion;

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

    public String getDeviceTypeVersion() {
        return deviceTypeVersion;
    }

    public void setDeviceTypeVersion(String deviceTypeVersion) {
        this.deviceTypeVersion = deviceTypeVersion;
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
}
