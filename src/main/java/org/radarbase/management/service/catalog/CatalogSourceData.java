package org.radarbase.management.service.catalog;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CatalogSourceData {
    @JsonProperty("app_provider")
    private String appProvider;

    @JsonProperty("processing_state")
    private String processingState;

    @JsonProperty
    private String type;

    @JsonProperty
    private String doc;

    @JsonProperty("sample_rate")
    private SampleRateConfig sampleRate;

    @JsonProperty
    private String unit;

    @JsonProperty
    private List<DataField> fields;

    private String topic;

    @JsonProperty("key_schema")
    private String keySchema;

    @JsonProperty("value_schema")
    private String valueSchema;

    private List<String> tags;

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
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

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getType() {
        return type;
    }

    public String getDoc() {
        return doc;
    }

    public SampleRateConfig getSampleRate() {
        return sampleRate;
    }

    public String getUnit() {
        return unit;
    }

    public List<CatalogSourceData.DataField> getFields() {
        return fields;
    }

    public String getProcessingState() {
        return processingState;
    }

    public void setProcessingState(String processingState) {
        this.processingState = processingState;
    }

    public void setAppProvider(String appProvider) {
        this.appProvider = appProvider;
    }

    public String getAppProvider() {
        return appProvider;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DataField {
        @JsonProperty
        private String name;

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return "DataField{" + "name='" + name + '\''
                    + '}';
        }
    }

    @Override
    public String toString() {
        return "CatalogSourceData{" + "appProvider='" + appProvider + '\''
                + ", processingState='" + processingState + '\''
                + ", type='" + type + '\''
                + ", doc='" + doc + '\''
                + ", sampleRate=" + sampleRate
                + ", unit='" + unit + '\''
                + ", fields=" + fields
                + ", topic='" + topic + '\''
                + ", keySchema='" + keySchema + '\''
                + ", valueSchema='" + valueSchema + '\''
                + ", tags=" + tags
                + '}';
    }
}
