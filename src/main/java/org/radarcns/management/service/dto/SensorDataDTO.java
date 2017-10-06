package org.radarcns.management.service.dto;


import org.radarcns.management.domain.enumeration.DataType;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * A DTO for the SensorData entity.
 */
public class SensorDataDTO implements Serializable {

    private Long id;

    //Sensor name.
    @NotNull
    private String sensorName;

    //Default data frequency
    private String frequency;

    //Measurement unit.
    private String unit;

    // Define if the samples are RAW data or instead they the result of some computation
    private DataType dataType;

    //  the storage
    private DataType dataClass;

    private String keySchema;

    private String valueSchema;

    private String topic;

    private String provider;

    private boolean enabled = true;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public String getSensorName() {
        return sensorName;
    }

    public void setSensorName(String sensorName) {
        this.sensorName = sensorName;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public DataType getDataType() {
        return dataType;
    }

    public void setDataType(DataType dataType) {
        this.dataType = dataType;
    }

    public DataType getDataClass() {
        return dataClass;
    }

    public void setDataClass(DataType dataClass) {
        this.dataClass = dataClass;
    }

    public String getKeySchema() {
        return keySchema;
    }

    public void setKeySchema(String keySchema) {
        this.keySchema = keySchema;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SensorDataDTO)) {
            return false;
        }

        SensorDataDTO that = (SensorDataDTO) o;

        if (enabled != that.enabled) {
            return false;
        }
        if (id != null ? !id.equals(that.id) : that.id != null) {
            return false;
        }
        if (sensorName != null ? !sensorName.equals(that.sensorName) : that.sensorName != null) {
            return false;
        }
        if (frequency != null ? !frequency.equals(that.frequency) : that.frequency != null) {
            return false;
        }
        if (unit != null ? !unit.equals(that.unit) : that.unit != null) {
            return false;
        }
        if (dataType != that.dataType) {
            return false;
        }
        if (dataClass != that.dataClass) {
            return false;
        }
        if (keySchema != null ? !keySchema.equals(that.keySchema) : that.keySchema != null) {
            return false;
        }
        if (valueSchema != null ? !valueSchema.equals(that.valueSchema) :
            that.valueSchema != null) {
            return false;
        }
        if (topic != null ? !topic.equals(that.topic) : that.topic != null) {
            return false;
        }
        return provider != null ? provider.equals(that.provider) : that.provider == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (sensorName != null ? sensorName.hashCode() : 0);
        result = 31 * result + (frequency != null ? frequency.hashCode() : 0);
        result = 31 * result + (unit != null ? unit.hashCode() : 0);
        result = 31 * result + (dataType != null ? dataType.hashCode() : 0);
        result = 31 * result + (dataClass != null ? dataClass.hashCode() : 0);
        result = 31 * result + (keySchema != null ? keySchema.hashCode() : 0);
        result = 31 * result + (valueSchema != null ? valueSchema.hashCode() : 0);
        result = 31 * result + (topic != null ? topic.hashCode() : 0);
        result = 31 * result + (provider != null ? provider.hashCode() : 0);
        result = 31 * result + (enabled ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "SensorDataDTO{" + "id=" + id + ", sensorName='" + sensorName + '\''
            + ", frequency='" + frequency + '\'' + ", unit='" + unit + '\'' + ", dataType="
            + dataType + ", dataClass=" + dataClass + ", keySchema='" + keySchema + '\''
            + ", valueSchema='" + valueSchema + '\'' + ", topic='" + topic + '\'' + ", provider='"
            + provider + '\'' + ", enabled=" + enabled + '}';
    }
}
