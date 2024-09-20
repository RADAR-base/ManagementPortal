package org.radarbase.management.web.rest.errors

import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable
import java.util.*

/**
 * View Model for sending a [RadarWebApplicationException].

 * Creates an error view model with message, entityName and errorCode.
 *
 * @param message message to client.
 * @param entityName entityRelated from [EntityName]
 * @param errorCode errorCode from [ErrorConstants]
 * @param params map of optional information.
 */
class RadarWebApplicationExceptionVM(
    @field:JsonProperty val message: String?,
    @field:JsonProperty val entityName: String,
    @field:JsonProperty val errorCode: String?,
    @field:JsonProperty val params: Map<String, String?>,
) : Serializable {
    /**
     * Creates an error view model with message, entityName and errorCode.
     *
     * @param message message to client.
     * @param entityName entityRelated from [EntityName]
     * @param errorCode errorCode from [ErrorConstants]
     */
    protected constructor(message: String?, entityName: String, errorCode: String?) : this(
        message,
        entityName,
        errorCode,
        emptyMap<String, String>(),
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || javaClass != other.javaClass) {
            return false
        }
        val that = other as RadarWebApplicationExceptionVM
        return entityName == that.entityName && errorCode == that.errorCode && message == that.message && params == that.params
    }

    override fun hashCode(): Int = Objects.hash(entityName, errorCode, message, params)

    override fun toString(): String =
        (
            "RadarWebApplicationExceptionVM{" + "entityName='" + entityName + '\'' +
                ", errorCode='" + errorCode + '\'' + ", message='" + message + '\'' + ", params=" +
                params + '}'
            )

    companion object {
        private const val serialVersionUID = 1L
    }
}
