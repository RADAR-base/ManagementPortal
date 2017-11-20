package org.radarcns.management.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.radarcns.management.domain.enumeration.ProcessingState;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * A SourceData.
 */
@Entity
@Table(name = "source_data")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class SourceData implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator", initialValue = 1000)
    private Long id;

    //SourceData type e.g. ACCELEROMETER, TEMPERATURE.
    @NotNull
    @Column(name = "source_data_type", nullable = false)
    private String sourceDataType;

    //Default data frequency
    @Column(name = "frequency")
    private String frequency;

    //Measurement unit.
    @Column(name = "unit")
    private String unit;

    // Define if the samples are RAW data or instead they the result of some computation
    @Enumerated(EnumType.STRING)
    @Column(name = "processing_state")
    private ProcessingState processingState;

    //  the storage
    @Column(name = "data_class")
    private String dataClass;

    @Column(name = "key_schema")
    private String keySchema;


    @Column(name = "value_schema")
    private String valueSchema;

    // source data topic
    @Column(name = "topic")
    private String topic;

    // app provider
    @Column(name = "provider")
    private String provider;

    @Column(name = "enabled")
    private boolean enabled = true;

    @ManyToMany(mappedBy = "sourceData" , fetch = FetchType.EAGER)
    @JsonIgnore
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    private Set<DeviceType> deviceTypes = new HashSet<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSourceDataType() {
        return sourceDataType;
    }

    public SourceData sourceDataType(String sourceDataType) {
        this.sourceDataType = sourceDataType;
        return this;
    }

    public void setSourceDataType(String sourceDataType) {
        this.sourceDataType = sourceDataType;
    }

    public ProcessingState getProcessingState() {
        return processingState;
    }

    public SourceData processingState(ProcessingState processingState) {
        this.processingState = processingState;
        return this;
    }

    public void setProcessingState(ProcessingState processingState) {
        this.processingState = processingState;
    }

    public String getKeySchema() {
        return keySchema;
    }

    public SourceData keySchema(String keySchema) {
        this.keySchema = keySchema;
        return this;
    }

    public void setKeySchema(String keySchema) {
        this.keySchema = keySchema;
    }

    public String getFrequency() {
        return frequency;
    }

    public SourceData frequency(String frequency) {
        this.frequency = frequency;
        return this;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public Set<DeviceType> getDeviceTypes() {
        return deviceTypes;
    }

    public SourceData deviceTypes(Set<DeviceType> deviceTypes) {
        this.deviceTypes = deviceTypes;
        return this;
    }

    public SourceData addDeviceType(DeviceType deviceType) {
        this.deviceTypes.add(deviceType);
        deviceType.getSourceData().add(this);
        return this;
    }

    public SourceData removeDeviceType(DeviceType deviceType) {
        this.deviceTypes.remove(deviceType);
        deviceType.getSourceData().remove(this);
        return this;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String  getDataClass() {
        return dataClass;
    }

    public void setDataClass(String dataClass) {
        this.dataClass = dataClass;
    }

    public String getValueSchema() {
        return valueSchema;
    }

    public void setValueSchema(String valueSchema) {
        this.valueSchema = valueSchema;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setDeviceTypes(Set<DeviceType> deviceTypes) {
        this.deviceTypes = deviceTypes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SourceData sourceData = (SourceData) o;
        if (sourceData.id == null || id == null) {
            return false;
        }
        return Objects.equals(id, sourceData.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "SourceData{" + "id=" + id + ", sourceDataType='" + sourceDataType + '\'' + ", frequency='"
            + frequency + '\'' + ", unit='" + unit + '\'' + ", processingState=" + processingState
            + ", dataClass='" + dataClass + '\'' + ", keySchema='" + keySchema + '\''
            + ", valueSchema='" + valueSchema + '\'' + ", topic='" + topic + '\'' + ", provider='"
            + provider + '\'' + ", enabled=" + enabled + ", deviceTypes=" + deviceTypes + '}';
    }
}
