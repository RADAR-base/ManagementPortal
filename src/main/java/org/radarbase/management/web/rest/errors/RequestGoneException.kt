package org.radarbase.management.web.rest.errors

import org.springframework.http.HttpStatus

/**
 * Throw when the requested resource is no longer available at the server and no forwarding
 * address is known. This condition is expected to be considered permanent. Clients with
 * link editing capabilities SHOULD delete references to the Request-URI after user approval.
 */
class RequestGoneException : RadarWebApplicationException {
    /**
     * Create a [RequestGoneException] with the given message, relatedEntityName, errorCode.
     *
     * @param message    the message.
     * @param entityName relatedEntityName from [EntityName].
     * @param errorCode  errorCode from [ErrorConstants]
     */
    constructor(message: String?, entityName: String, errorCode: String?) : super(
        HttpStatus.GONE,
        message,
        entityName,
        errorCode,
    )

    /**
     * Create a [RequestGoneException] with the given message, relatedEntityName, errorCode.
     *
     * @param message    the message.
     * @param entityName relatedEntityName from [EntityName].
     * @param errorCode  errorCode from [ErrorConstants]
     * @param paramMap   map of additional information.
     */
    constructor(
        message: String?,
        entityName: String,
        errorCode: String?,
        paramMap: Map<String, String?>?,
    ) : super(HttpStatus.GONE, message, entityName, errorCode, paramMap)
}
