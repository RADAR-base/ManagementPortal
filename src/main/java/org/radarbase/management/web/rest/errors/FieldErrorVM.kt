package org.radarbase.management.web.rest.errors

import java.io.Serializable

class FieldErrorVM
/**
 * Create a new field error view-model.
 * @param dto the object name
 * @param field the field name
 * @param message the message
 */(val objectName: String?, val field: String?, val message: String?) : Serializable {

    companion object {
        private const val serialVersionUID = 1L
    }
}
