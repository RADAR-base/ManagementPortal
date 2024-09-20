package org.radarbase.management.web.rest.errors

import org.springframework.http.HttpStatus

/**
 * The request could not be understood by the server due to malformed syntax.
 * The client SHOULD NOT repeat the request without modifications.
 * (e.g. malformed request syntax, size too large, invalid request message framing,
 * or deceptive request routing).
 */
class BadRequestException : RadarWebApplicationException {
    /**
     * Create a BadRequestException with the given message, relatedEntityName, errorCode.
     *
     * @param message    the message.
     * @param entityName relatedEntityName from [EntityName].
     * @param errorCode  errorCode from [ErrorConstants]
     */
    constructor(message: String?, entityName: String, errorCode: String?) : super(
        HttpStatus.BAD_REQUEST,
        message,
        entityName,
        errorCode,
    )

    /**
     * Create a BadRequestException with the given message, relatedEntityName, errorCode.
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
    ) : super(HttpStatus.BAD_REQUEST, message, entityName, errorCode, paramMap)
}
