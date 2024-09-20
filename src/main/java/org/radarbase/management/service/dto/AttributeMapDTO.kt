package org.radarbase.management.service.dto

import java.util.*

/**
 * Created by nivethika on 30-8-17.
 */
class AttributeMapDTO
    @JvmOverloads
    constructor(
        var key: String? = null,
        var value: String? = null,
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) {
                return true
            }
            if (other == null || javaClass != other.javaClass) {
                return false
            }
            val attributeMapDto = other as AttributeMapDTO
            return key == attributeMapDto.key && value == attributeMapDto.value
        }

        override fun hashCode(): Int = Objects.hash(key, value)

        override fun toString(): String =
            (
                "AttributeMapDTO{" +
                    " key='" + key + "'" +
                    ", value='" + value + "'" +
                    '}'
                )
    }
