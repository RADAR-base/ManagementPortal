package org.radarbase.management.web.rest.errors

import java.io.Serializable

/**
 * View Model for transferring error message with a list of field errors.
 */
class ErrorVM @JvmOverloads constructor(val message: String?, val description: String? = null) : Serializable {
    private var fieldErrors: MutableList<FieldErrorVM>? = null

    /**
     * Add a field error.
     * @param objectName the object name
     * @param field the field name
     * @param message the error message
     */
    fun add(objectName: String?, field: String?, message: String?) {
        if (fieldErrors == null) {
            fieldErrors = ArrayList()
        }
        fieldErrors!!.add(FieldErrorVM(objectName, field, message))
    }

    fun getFieldErrors(): List<FieldErrorVM>? {
        return fieldErrors
    }

    companion object {
        private const val serialVersionUID = 1L
    }
}
