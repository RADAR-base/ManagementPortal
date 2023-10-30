package org.radarbase.management.service.dto

import java.util.*

/**
 * Created by nivethika on 30-8-17.
 */
class AttributeMapDTO @JvmOverloads constructor(var key: String? = null, var value: String? = null) {

    override fun equals(o: Any?): Boolean {
        if (this === o) {
            return true
        }
        if (o == null || javaClass != o.javaClass) {
            return false
        }
        val attributeMapDto = o as AttributeMapDTO
        return key == attributeMapDto.key && value == attributeMapDto.value
    }

    override fun hashCode(): Int {
        return Objects.hash(key, value)
    }

    override fun toString(): String {
        return ("AttributeMapDTO{"
                + " key='" + key + "'"
                + ", value='" + value + "'"
                + '}')
    }
}
