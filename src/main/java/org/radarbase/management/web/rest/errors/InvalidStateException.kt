package org.radarbase.management.web.rest.errors

import org.springframework.http.HttpStatus

/**
 * The server encountered an unexpected condition which prevented it from fulfilling the request.
 */
class InvalidStateException : RadarWebApplicationException {
    /**
     * Create a [InvalidStateException] with the given message, relatedEntityName, errorCode.
     *
     * @param message    the message.
     * @param entityName relatedEntityName from [EntityName].
     * @param errorCode  errorCode from [ErrorConstants]
     */
    constructor(message: String?, entityName: String, errorCode: String?) : super(
        HttpStatus.INTERNAL_SERVER_ERROR,
        message,
        entityName,
        errorCode
    )

    /**
     * Create a [InvalidStateException] with the given message, relatedEntityName, errorCode.
     *
     * @param message    the message.
     * @param entityName relatedEntityName from [EntityName].
     * @param errorCode  errorCode from [ErrorConstants]
     * @param params     map of additional information.
     */
    constructor(
        message: String?, entityName: String, errorCode: String?,
        params: Map<String, String?>?
    ) : super(HttpStatus.INTERNAL_SERVER_ERROR, message, entityName, errorCode, params)
}
