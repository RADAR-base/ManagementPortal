package org.radarcns.management.service.catalog;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class SourceTypeResponse {

    @JsonProperty("passive-source-types")
    private List<CatalogSourceType> passiveSources;

    @JsonProperty("active-source-types")
    private List<CatalogSourceType> activeSources;

    @JsonProperty("monitor-source-types")
    private List<CatalogSourceType> monitorSources;

    public List<CatalogSourceType> getPassiveSources() {
        return passiveSources;
    }

    public List<CatalogSourceType> getActiveSources() {
        return activeSources;
    }

    public List<CatalogSourceType> getMonitorSources() {
        return monitorSources;
    }
}
