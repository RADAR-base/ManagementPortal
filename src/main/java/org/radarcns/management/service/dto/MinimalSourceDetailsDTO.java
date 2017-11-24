package org.radarcns.management.service.dto;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by nivethika on 13-6-17.
 */
public class MinimalSourceDetailsDTO {
    private Long id;
    private Long sourceTypeId;
    private String sourceTypeProducer;
    private String sourceTypeModel;
    private String sourceTypeCatalogVersion;
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

    public Long getSourceTypeId() {
        return sourceTypeId;
    }

    public void setSourceTypeId(Long sourceTypeId) {
        this.sourceTypeId = sourceTypeId;
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

    public String getSourceTypeCatalogVersion() {
        return sourceTypeCatalogVersion;
    }

    public void setSourceTypeCatalogVersion(String sourceTypeCatalogVersion) {
        this.sourceTypeCatalogVersion = sourceTypeCatalogVersion;
    }

    public String getSourceTypeModel() {
        return sourceTypeModel;
    }

    public void setSourceTypeModel(String sourceTypeModel) {
        this.sourceTypeModel = sourceTypeModel;
    }

    public String getSourceTypeProducer() {
        return sourceTypeProducer;
    }

    public void setSourceTypeProducer(String sourceTypeProducer) {
        this.sourceTypeProducer = sourceTypeProducer;
    }

    @Override
    public String toString() {
        return "MinimalSourceDetailsDTO{"
                + "id=" + id
                + ", sourceTypeId=" + sourceTypeId
                + ", sourceTypeProducer='" + sourceTypeProducer + '\''
                + ", sourceTypeModel='" + sourceTypeModel + '\''
                + ", sourceTypeCatalogVersion='" + sourceTypeCatalogVersion + '\''
                + ", expectedSourceName='" + expectedSourceName + '\''
                + ", sourceId=" + sourceId
                + ", sourceName='" + sourceName + '\''
                + ", assigned=" + assigned
                + ", attributes=" + attributes + '}';
    }
}
