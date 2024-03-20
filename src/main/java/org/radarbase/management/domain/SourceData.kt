package org.radarbase.management.domain

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import org.hibernate.envers.Audited
import org.radarbase.management.domain.support.AbstractEntityListener
import org.radarbase.management.security.Constants
import java.io.Serializable
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EntityListeners
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.ManyToOne
import javax.persistence.SequenceGenerator
import javax.persistence.Table
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern

/**
 * A SourceData.
 */
@Entity
@Audited
@Table(name = "source_data")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@EntityListeners(
    AbstractEntityListener::class
)
class SourceData : AbstractEntity(), Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator", initialValue = 1000, sequenceName = "hibernate_sequence")
    override var id: Long? = null

    //SourceData type e.g. ACCELEROMETER, TEMPERATURE.
    @JvmField
    @Column(name = "source_data_type", nullable = false)
    @NotNull var sourceDataType: String? = null

    // this will be the unique human readable identifier of
    @JvmField
    @Column(name = "source_data_name", nullable = false, unique = true)
    @NotNull @Pattern(regexp = Constants.ENTITY_ID_REGEX) var sourceDataName: String? = null

    //Default data frequency
    @JvmField
    @Column(name = "frequency")
    var frequency: String? = null

    //Measurement unit.
    @JvmField
    @Column(name = "unit")
    var unit: String? = null

    // Define if the samples are RAW data or instead they the result of some computation
    @JvmField
    @Column(name = "processing_state")
    var processingState: String? = null

    //  the storage
    @JvmField
    @Column(name = "data_class")
    var dataClass: String? = null

    @JvmField
    @Column(name = "key_schema")
    var keySchema: String? = null

    @JvmField
    @Column(name = "value_schema")
    var valueSchema: String? = null

    // source data topic
    @JvmField
    @Column(name = "topic")
    var topic: String? = null

    // app provider
    @JvmField
    @Column(name = "provider")
    var provider: String? = null

    @Column(name = "enabled")
    var enabled = true

    @JvmField
    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties("sourceData") // avoids infinite recursion in JSON serialization
    var sourceType: SourceType? = null
    fun sourceDataType(sourceDataType: String?): SourceData {
        this.sourceDataType = sourceDataType
        return this
    }

    fun sourceDataName(sourceDataName: String?): SourceData {
        this.sourceDataName = sourceDataName
        return this
    }

    fun processingState(processingState: String?): SourceData {
        this.processingState = processingState
        return this
    }

    fun keySchema(keySchema: String?): SourceData {
        this.keySchema = keySchema
        return this
    }

    fun frequency(frequency: String?): SourceData {
        this.frequency = frequency
        return this
    }

    fun sourceType(sourceType: SourceType?): SourceData {
        this.sourceType = sourceType
        return this
    }

    fun unit(unit: String?): SourceData {
        this.unit = unit
        return this
    }

    fun valueSchema(valueSchema: String?): SourceData {
        this.valueSchema = valueSchema
        return this
    }

    fun topic(topic: String?): SourceData {
        this.topic = topic
        return this
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || javaClass != other.javaClass) {
            return false
        }
        val sourceData = other as SourceData
        return if (sourceData.id == null || id == null) {
            false
        } else id == sourceData.id
    }

    override fun hashCode(): Int {
        return Objects.hashCode(id)
    }

    override fun toString(): String {
        return ("SourceData{" + "id=" + id + ", sourceDataType='" + sourceDataType + '\''
                + ", frequency='"
                + frequency + '\'' + ", unit='" + unit + '\'' + ", processingState="
                + processingState
                + ", dataClass='" + dataClass + '\'' + ", keySchema='" + keySchema + '\''
                + ", valueSchema='" + valueSchema + '\'' + ", topic='" + topic + '\''
                + ", provider='"
                + provider + '\'' + ", enabled=" + enabled + '}')
    }

    companion object {
        private const val serialVersionUID = 1L
    }
}
