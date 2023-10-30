package org.radarbase.management.service.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.annotation.Nulls
import java.io.Serializable
import java.util.*
import javax.validation.constraints.NotNull

/**
 * A DTO for the SourceType entity.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
class SourceTypeDTO : Serializable {
    var id: Long? = null
    lateinit var producer: @NotNull String
    lateinit var model: @NotNull String
    lateinit var catalogVersion: @NotNull String
    lateinit var sourceTypeScope: @NotNull String
    var canRegisterDynamically: @NotNull Boolean = false
    var name: String? = null
    var description: String? = null
    var assessmentType: String? = null
    var appProvider: String? = null

    @set:JsonSetter(nulls = Nulls.AS_EMPTY)
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    var sourceData: Set<SourceDataDTO> = HashSet()
    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || javaClass != other.javaClass) {
            return false
        }
        val sourceTypeDto = other as SourceTypeDTO
        return if (id == null || sourceTypeDto.id == null) {
            false
        } else id == sourceTypeDto.id
    }

    override fun hashCode(): Int {
        return Objects.hashCode(id)
    }

    override fun toString(): String {
        return ("SourceTypeDTO{"
                + "id=" + id
                + ", producer='" + producer + "'"
                + ", model='" + model + "'"
                + ", catalogVersion='" + catalogVersion + "'"
                + ", sourceTypeScope='" + sourceTypeScope + "'"
                + ", canRegisterDynamically='" + canRegisterDynamically + "'"
                + ", name='" + name + '\''
                + ", description=" + description
                + ", appProvider=" + appProvider
                + ", assessmentType=" + assessmentType
                + '}')
    }

    companion object {
        private const val serialVersionUID = 1L
    }
}
