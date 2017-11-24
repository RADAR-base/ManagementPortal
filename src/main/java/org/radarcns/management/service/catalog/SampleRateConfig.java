package org.radarcns.management.service.catalog;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SampleRateConfig {
    @JsonProperty
    private Double interval;

    @JsonProperty
    private Double frequency;

    @JsonProperty
    private boolean dynamic;

    @JsonProperty
    private boolean configurable;

    public Double getInterval() {
        return interval;
    }

    public Double getFrequency() {
        return frequency;
    }

    public boolean isDynamic() {
        return dynamic;
    }

    public boolean isConfigurable() {
        return configurable;
    }

    @Override
    public String toString() {
        return "SampleRateConfig{interval=" + interval
                + ", frequency=" + frequency
                + ", dynamic=" + dynamic
                + ", configurable=" + configurable
                + '}';
    }
}
