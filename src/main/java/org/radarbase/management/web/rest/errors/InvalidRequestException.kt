package org.radarbase.management.web.rest.errors

import org.springframework.http.HttpStatus

/**
 * The server understood the request, but is refusing to fulfill it.
 * Authorization will not help and the request SHOULD NOT be repeated.
 */
class InvalidRequestException : RadarWebApplicationException {
    /**
     * Create a [InvalidRequestException] with the given message, relatedEntityName,errorCode.
     *
     * @param message    the message.
     * @param entityName relatedEntityName from [EntityName].
     * @param errorCode  errorCode from [ErrorConstants]
     */
    constructor(message: String?, entityName: String, errorCode: String?) : super(
        HttpStatus.FORBIDDEN,
        message,
        entityName,
        errorCode
    )

    /**
     * Create a [InvalidRequestException] with the given message, relatedEntityName,errorCode.
     *
     * @param message    the message.
     * @param entityName relatedEntityName from [EntityName].
     * @param errorCode  errorCode from [ErrorConstants]
     * @param params     map of additional information.
     */
    constructor(
        message: String?, entityName: String, errorCode: String?,
        params: Map<String, String?>?
    ) : super(HttpStatus.FORBIDDEN, message, entityName, errorCode, params)
}
