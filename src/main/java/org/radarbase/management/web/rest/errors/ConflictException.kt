package org.radarbase.management.web.rest.errors

import org.springframework.http.HttpStatus

/**
 * Created by dverbeec on 13/09/2017.
 *
 *
 * The request could not be completed due to a conflict with the current state of the resource.
 * This code is only allowed in situations where it is expected that the user might be able to
 * resolve the conflict and resubmit the request. The response body SHOULD include enough
 * information for the user to recognize the source of the conflict. Ideally, the response
 * entity would include enough information for the user or user agent to fix the problem;
 * however, that might not be possible and is not required.
 *
 */
class ConflictException : RadarWebApplicationException {
    /**
     * Create a [ConflictException] with the given message, relatedEntityName, errorCode.
     *
     * @param message    the message.
     * @param entityName relatedEntityName from [EntityName].
     * @param errorCode  errorCode from [ErrorConstants]
     */
    constructor(message: String?, entityName: String, errorCode: String?) : super(
        HttpStatus.CONFLICT,
        message,
        entityName,
        errorCode
    )

    /**
     * Create a [ConflictException] with the given message, relatedEntityName, errorCode.
     *
     * @param message    the message.
     * @param entityName relatedEntityName from [EntityName].
     * @param errorCode  errorCode from [ErrorConstants]
     * @param paramMap   map of additional information.
     */
    constructor(
        message: String?, entityName: String, errorCode: String?,
        paramMap: Map<String, String?>?
    ) : super(HttpStatus.CONFLICT, message, entityName, errorCode, paramMap)
}
