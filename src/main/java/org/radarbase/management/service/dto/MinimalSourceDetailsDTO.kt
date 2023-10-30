package org.radarbase.management.service.dto

import java.util.*

/**
 * Created by nivethika on 13-6-17.
 */
class MinimalSourceDetailsDTO {
    var id: Long? = null
    var sourceTypeId: Long? = null
    var sourceTypeProducer: String? = null
    var sourceTypeModel: String? = null
    var sourceTypeCatalogVersion: String? = null
    var expectedSourceName: String? = null
        private set
    lateinit var sourceId: UUID
    var sourceName: String? = null
    var isAssigned: Boolean? = null
    var attributes: Map<String, String> = HashMap()
    fun id(id: Long?): MinimalSourceDetailsDTO {
        this.id = id
        return this
    }

    fun sourceTypeId(sourceTypeId: Long?): MinimalSourceDetailsDTO {
        this.sourceTypeId = sourceTypeId
        return this
    }

    fun setExpectedSourceName(expectedSourceName: String?): MinimalSourceDetailsDTO {
        this.expectedSourceName = expectedSourceName
        return this
    }

    fun sourceId(sourceId: UUID): MinimalSourceDetailsDTO {
        this.sourceId = sourceId
        return this
    }

    fun assigned(assigned: Boolean?): MinimalSourceDetailsDTO {
        isAssigned = assigned
        return this
    }

    fun sourceName(sourceName: String?): MinimalSourceDetailsDTO {
        this.sourceName = sourceName
        return this
    }

    fun attributes(attributes: Map<String, String>): MinimalSourceDetailsDTO {
        this.attributes = attributes
        return this
    }

    fun sourceTypeCatalogVersion(sourceTypeCatalogVersion: String?): MinimalSourceDetailsDTO {
        this.sourceTypeCatalogVersion = sourceTypeCatalogVersion
        return this
    }

    fun sourceTypeModel(sourceTypeModel: String?): MinimalSourceDetailsDTO {
        this.sourceTypeModel = sourceTypeModel
        return this
    }

    fun sourceTypeProducer(sourceTypeProducer: String?): MinimalSourceDetailsDTO {
        this.sourceTypeProducer = sourceTypeProducer
        return this
    }

    override fun toString(): String {
        return ("MinimalSourceDetailsDTO{"
                + "id=" + id
                + ", sourceTypeId=" + sourceTypeId
                + ", sourceTypeProducer='" + sourceTypeProducer + '\''
                + ", sourceTypeModel='" + sourceTypeModel + '\''
                + ", sourceTypeCatalogVersion='" + sourceTypeCatalogVersion + '\''
                + ", expectedSourceName='" + expectedSourceName + '\''
                + ", sourceId=" + sourceId
                + ", sourceName='" + sourceName + '\''
                + ", assigned=" + isAssigned
                + ", attributes=" + attributes + '}')
    }
}
