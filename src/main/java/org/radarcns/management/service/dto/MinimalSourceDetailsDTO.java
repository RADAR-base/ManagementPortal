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
    private Boolean assigned;
    private Map<String, String> attributes = new HashMap<>();

    public Long getId() {
        return id;
    }

    public MinimalSourceDetailsDTO id(Long id) {
        this.id = id;
        return this;
    }

    public Long getSourceTypeId() {
        return sourceTypeId;
    }

    public MinimalSourceDetailsDTO sourceTypeId(Long sourceTypeId) {
        this.sourceTypeId = sourceTypeId;
        return this;
    }

    public String getExpectedSourceName() {
        return expectedSourceName;
    }

    public MinimalSourceDetailsDTO setExpectedSourceName(String expectedSourceName) {
        this.expectedSourceName = expectedSourceName;
        return this;
    }

    public UUID getSourceId() {
        return sourceId;
    }

    public MinimalSourceDetailsDTO sourceId(UUID sourceId) {
        this.sourceId = sourceId;
        return this;
    }

    public Boolean isAssigned() {
        return assigned;
    }

    public MinimalSourceDetailsDTO assigned(Boolean assigned) {
        this.assigned = assigned;
        return this;
    }

    public String getSourceName() {
        return sourceName;
    }

    public MinimalSourceDetailsDTO sourceName(String sourceName) {
        this.sourceName = sourceName;
        return this;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public MinimalSourceDetailsDTO attributes(Map<String, String> attributes) {
        this.attributes = attributes;
        return this;
    }

    public String getSourceTypeCatalogVersion() {
        return sourceTypeCatalogVersion;
    }

    public MinimalSourceDetailsDTO sourceTypeCatalogVersion(String sourceTypeCatalogVersion) {
        this.sourceTypeCatalogVersion = sourceTypeCatalogVersion;
        return this;
    }

    public String getSourceTypeModel() {
        return sourceTypeModel;
    }

    public MinimalSourceDetailsDTO sourceTypeModel(String sourceTypeModel) {
        this.sourceTypeModel = sourceTypeModel;
        return this;
    }

    public String getSourceTypeProducer() {
        return sourceTypeProducer;
    }

    public MinimalSourceDetailsDTO sourceTypeProducer(String sourceTypeProducer) {
        this.sourceTypeProducer = sourceTypeProducer;
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setSourceTypeId(Long sourceTypeId) {
        this.sourceTypeId = sourceTypeId;
    }

    public void setSourceTypeProducer(String sourceTypeProducer) {
        this.sourceTypeProducer = sourceTypeProducer;
    }

    public void setSourceTypeModel(String sourceTypeModel) {
        this.sourceTypeModel = sourceTypeModel;
    }

    public void setSourceTypeCatalogVersion(String sourceTypeCatalogVersion) {
        this.sourceTypeCatalogVersion = sourceTypeCatalogVersion;
    }

    public void setSourceId(UUID sourceId) {
        this.sourceId = sourceId;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public void setAssigned(Boolean assigned) {
        this.assigned = assigned;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
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
