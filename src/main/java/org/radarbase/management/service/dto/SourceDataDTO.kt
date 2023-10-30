package org.radarbase.management.service.dto

import com.fasterxml.jackson.annotation.JsonInclude
import java.io.Serializable
import java.util.*

/**
 * A DTO for the SourceData entity.
 */
class SourceDataDTO : Serializable {
    var id: Long? = null

    //Source data type.
    var sourceDataType: String? = null
    var sourceDataName: String? = null

    //Default data frequency
    var frequency: String? = null

    //Measurement unit.
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

    override fun equals(o: Any?): Boolean {
        if (this === o) {
            return true
        }
        if (o == null || javaClass != o.javaClass) {
            return false
        }
        val sourceDataDto = o as SourceDataDTO
        return if (sourceDataDto.id == null || id == null) {
            false
        } else id == sourceDataDto.id
    }

    override fun hashCode(): Int {
        return Objects.hashCode(id)
    }

    override fun toString(): String {
        return ("SourceDataDTO{"
                + "id=" + id
                + ", sourceDataType='" + sourceDataType + '\''
                + ", sourceDataName='" + sourceDataName + '\''
                + ", frequency='" + frequency + '\''
                + ", unit='" + unit + '\''
                + ", processingState=" + processingState
                + ", dataClass=" + dataClass
                + ", keySchema='" + keySchema + '\''
                + ", valueSchema='" + valueSchema + '\''
                + ", topic='" + topic + '\''
                + ", provider='" + provider + '\''
                + ", enabled=" + isEnabled
                + '}')
    }

    companion object {
        private const val serialVersionUID = 1L
    }
}
