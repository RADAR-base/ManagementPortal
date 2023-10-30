package org.radarbase.management.service.catalog

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
class SampleRateConfig {
    @JsonProperty
    val interval: Double? = null

    @JsonProperty
    val frequency: Double? = null

    @JsonProperty
    val isDynamic = false

    @JsonProperty
    val isConfigurable = false
    override fun toString(): String {
        return ("SampleRateConfig{interval=" + interval
                + ", frequency=" + frequency
                + ", dynamic=" + isDynamic
                + ", configurable=" + isConfigurable
                + '}')
    }
}
