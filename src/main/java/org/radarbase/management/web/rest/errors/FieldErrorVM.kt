package org.radarbase.management.web.rest.errors

import java.io.Serializable

/**
 * Create a new field error view-model.
 * @param dto the object name
 * @param field the field name
 * @param message the message
 */
class FieldErrorVM(
    val objectName: String?,
    val field: String?,
    val message: String?,
) : Serializable {
    companion object {
        private const val serialVersionUID = 1L
    }
}
