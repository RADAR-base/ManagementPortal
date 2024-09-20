package org.radarbase.management.web.rest.errors

import org.springframework.http.HttpStatus

/**
 * Created by dverbeec on 7/09/2017.
 * Modified by nivethika on 2/08/2018.
 *
 * The server has not found anything matching the Request-URI.
 * No indication is given of whether the condition is temporary or permanent.
 *
 */
class NotFoundException : RadarWebApplicationException {
    /**
     * Create a [NotFoundException] with the given message, relatedEntityName, errorCode.
     *
     * @param message    the message.
     * @param entityName relatedEntityName from [EntityName].
     * @param errorCode  errorCode from [ErrorConstants]
     */
    constructor(message: String?, entityName: String, errorCode: String?) : super(
        HttpStatus.NOT_FOUND,
        message,
        entityName,
        errorCode,
    )

    /**
     * Create a [NotFoundException] with the given message, relatedEntityName, errorCode.
     *
     * @param message    the message.
     * @param entityName relatedEntityName from [EntityName].
     * @param errorCode  errorCode from [ErrorConstants]
     * @param paramMap   map of additional information.
     */
    constructor(
        message: String,
        entityName: String,
        errorCode: String,
        paramMap: Map<String, String?>?,
    ) : super(HttpStatus.NOT_FOUND, message, entityName, errorCode, paramMap)
}
