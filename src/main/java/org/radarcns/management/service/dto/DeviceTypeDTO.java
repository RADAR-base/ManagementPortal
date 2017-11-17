package org.radarcns.management.service.dto;


import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import javax.validation.constraints.NotNull;
import org.radarcns.management.domain.enumeration.SourceTypeScope;

/**
 * A DTO for the DeviceType entity.
 */
public class DeviceTypeDTO implements Serializable {

    private Long id;

    @NotNull
    private String deviceProducer;

    @NotNull
    private String deviceModel;

    @NotNull
    private String catalogVersion;

    @NotNull
    private SourceTypeScope sourceTypeScope;

    @NotNull
    private Boolean canRegisterDynamically = false;

    private String name;

    private String description;

    private String assessmentType;

    private String appProvider;

    private Set<SourceDataDTO> sourceData = new HashSet<>();

    private DeviceTypeId deviceTypeId;

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

    public String getCatalogVersion() {
        return catalogVersion;
    }

    public void setCatalogVersion(String catalogVersion) {
        this.catalogVersion = catalogVersion;
    }

    public void setDeviceModel(String deviceModel) {
        this.deviceModel = deviceModel;
    }
    public SourceTypeScope getSourceTypeScope() {
        return sourceTypeScope;
    }

    public void setSourceTypeScope(SourceTypeScope sourceTypeScope) {
        this.sourceTypeScope = sourceTypeScope;
    }

    public Set<SourceDataDTO> getSourceData() {
        return sourceData;
    }

    public void setSourceData(Set<SourceDataDTO> sourceData) {
        this.sourceData = sourceData;
    }

    public Boolean getCanRegisterDynamically() {
        return canRegisterDynamically;
    }

    public void setCanRegisterDynamically(Boolean canRegisterDynamically) {
        this.canRegisterDynamically = canRegisterDynamically;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAssessmentType() {
        return assessmentType;
    }

    public void setAssessmentType(String assessmentType) {
        this.assessmentType = assessmentType;
    }

    public String getAppProvider() {
        return appProvider;
    }

    public void setAppProvider(String appProvider) {
        this.appProvider = appProvider;
    }

    public void initId() {
        this.deviceTypeId = new DeviceTypeId().producer(deviceProducer)
                                              .model(deviceModel)
                                              .version(catalogVersion);
    }

    public DeviceTypeId getDeviceTypeId() {
        return deviceTypeId;
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
            ", catalogVersion='" + catalogVersion + "'" +
            ", sourceTypeScope='" + sourceTypeScope + "'" +
            ", canRegisterDynamically='" + canRegisterDynamically + "'" +
            ", name='" + name + '\'' +
            ", description=" + description +
            ", appProvider=" + appProvider +
            ", assessmentType=" + assessmentType +
            '}';
    }

    private class DeviceTypeId {
        private String producer;
        private String model;
        private String version;

        public DeviceTypeId() {
        }

        public DeviceTypeId producer(String producer) {
            this.producer = producer;
            return this;
        }

        public DeviceTypeId model(String model) {
            this.model = model;
            return this;
        }

        public DeviceTypeId version(String version) {
            this.version = version;
            return this;
        }

        public String getProducer() {
            return producer;
        }

        public String getModel() {
            return model;
        }

        public String getVersion() {
            return version;
        }
    }
}
