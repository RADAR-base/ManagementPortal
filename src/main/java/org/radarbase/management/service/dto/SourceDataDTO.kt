package org.radarbase.management.service.dto

import com.fasterxml.jackson.annotation.JsonInclude
import java.io.Serializable
import java.util.*

/**
 * A DTO for the SourceData entity.
 */
class SourceDataDTO : Serializable {
    var id: Long? = null

    /** Source data type. Defaults to the topic of the source data. */
    var sourceDataType: String? = null
        get() = field ?: topic

    var sourceDataName: String? = null

    // Default data frequency
    var frequency: String? = null

    // Measurement unit.
    var unit: String? = null

    // Define if the samples are RAW data or the result of some computation
    var processingState: String? = null

    //  the storage
    var dataClass: String? = null
    var keySchema: String? = null
    var valueSchema: String? = null
    var topic: String? = null
    var provider: String? = null
    var isEnabled = true

    @JsonInclude(JsonInclude.Include.NON_NULL)
    var sourceType: MinimalSourceTypeDTO? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || javaClass != other.javaClass) {
            return false
        }
        val sourceDataDto = other as SourceDataDTO
        return if (sourceDataDto.id == null || id == null) {
            false
        } else {
            id == sourceDataDto.id
        }
    }

    override fun hashCode(): Int = Objects.hashCode(id)

    override fun toString(): String =
        (
            "SourceDataDTO{" +
                "id=" + id +
                ", sourceDataType='" + sourceDataType + '\'' +
                ", sourceDataName='" + sourceDataName + '\'' +
                ", frequency='" + frequency + '\'' +
                ", unit='" + unit + '\'' +
                ", processingState=" + processingState +
                ", dataClass=" + dataClass +
                ", keySchema='" + keySchema + '\'' +
                ", valueSchema='" + valueSchema + '\'' +
                ", topic='" + topic + '\'' +
                ", provider='" + provider + '\'' +
                ", enabled=" + isEnabled +
                '}'
            )

    companion object {
        private const val serialVersionUID = 1L
    }
}
