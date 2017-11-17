package org.radarcns.management.service.dto;


import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import javax.validation.constraints.NotNull;
import org.radarcns.management.domain.enumeration.SourceType;

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
    private SourceType sourceType;

    @NotNull
    private Boolean canRegisterDynamically = false;

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
    public SourceType getSourceType() {
        return sourceType;
    }

    public void setSourceType(SourceType sourceType) {
        this.sourceType = sourceType;
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
            ", sourceType='" + sourceType + "'" +
            ", canRegisterDynamically='" + canRegisterDynamically + "'" +
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
