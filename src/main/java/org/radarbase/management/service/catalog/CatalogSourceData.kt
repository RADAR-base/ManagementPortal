package org.radarbase.management.service.catalog

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
class CatalogSourceData {
    @JsonProperty("app_provider")
    var appProvider: String? = null

    @JsonProperty("processing_state")
    var processingState: String? = null

    @JsonProperty
    val type: String? = null

    @JsonProperty
    val doc: String? = null

    @JsonProperty("sample_rate")
    val sampleRate: SampleRateConfig? = null

    @JsonProperty
    val unit: String? = null

    @JsonProperty
    val fields: List<DataField>? = null
    var topic: String? = null

    @JsonProperty("key_schema")
    var keySchema: String? = null

    @JsonProperty("value_schema")
    var valueSchema: String? = null
    var tags: List<String>? = null

    @JsonIgnoreProperties(ignoreUnknown = true)
    class DataField {
        @JsonProperty
        val name: String? = null

        override fun toString(): String =
            (
                "DataField{" + "name='" + name + '\'' +
                    '}'
                )
    }

    override fun toString(): String =
        (
            "CatalogSourceData{" + "appProvider='" + appProvider + '\'' +
                ", processingState='" + processingState + '\'' +
                ", type='" + type + '\'' +
                ", doc='" + doc + '\'' +
                ", sampleRate=" + sampleRate +
                ", unit='" + unit + '\'' +
                ", fields=" + fields +
                ", topic='" + topic + '\'' +
                ", keySchema='" + keySchema + '\'' +
                ", valueSchema='" + valueSchema + '\'' +
                ", tags=" + tags +
                '}'
            )
}
