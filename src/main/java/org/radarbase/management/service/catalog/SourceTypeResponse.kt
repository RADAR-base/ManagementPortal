package org.radarbase.management.service.catalog

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
class SourceTypeResponse {
    @JsonProperty("passive-source-types")
    val passiveSources: List<CatalogSourceType>? = null

    @JsonProperty("active-source-types")
    val activeSources: List<CatalogSourceType>? = null

    @JsonProperty("monitor-source-types")
    val monitorSources: List<CatalogSourceType>? = null

    @JsonProperty("connector-source-types")
    val connectorSources: List<CatalogSourceType>? = null
}
