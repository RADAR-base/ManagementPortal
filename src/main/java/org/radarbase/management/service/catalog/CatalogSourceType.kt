package org.radarbase.management.service.catalog

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import org.hibernate.validator.constraints.NotEmpty
import java.util.*

@JsonIgnoreProperties(ignoreUnknown = true)
class CatalogSourceType {
    @JsonProperty("assessment_type")
    val assessmentType: String? = null

    @JsonProperty("app_provider")
    val appProvider: String? = null

    @JsonProperty
    val vendor: String? = null

    @JsonProperty
    val model: String? = null

    @JsonProperty
    val version: String? = null

    @JsonProperty
    val name: String? = null

    @JsonProperty
    val doc: String? = null

    @JsonProperty
    val scope: String? = null

    @JsonProperty
    val properties: Map<String, String> = emptyMap()

    @JsonProperty
    val data: @NotEmpty MutableList<CatalogSourceData>? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || javaClass != other.javaClass) {
            return false
        }
        val that = other as CatalogSourceType
        return assessmentType == that.assessmentType &&
            appProvider == that.appProvider &&
            vendor == that.vendor &&
            model == that.model &&
            version == that.version &&
            name == that.name &&
            doc == that.doc &&
            scope == that.scope &&
            properties == that.properties &&
            data == that.data
    }

    override fun hashCode(): Int =
        Objects
            .hash(
                assessmentType,
                appProvider,
                vendor,
                model,
                version,
                name,
                doc,
                scope,
                properties,
                data,
            )
}
