package org.radarbase.management.web.rest.errors;

import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * Throw when the requested resource is no longer available at the server and no forwarding
 * address is known. This condition is expected to be considered permanent. Clients with
 * link editing capabilities SHOULD delete references to the Request-URI after user approval.
 */
public class RequestGoneException extends RadarWebApplicationException {

    /**
     * Create a {@link RequestGoneException} with the given message, relatedEntityName, errorCode.
     *
     * @param message    the message.
     * @param entityName relatedEntityName from {@link EntityName}.
     * @param errorCode  errorCode from {@link ErrorConstants}
     */
    public RequestGoneException(String message, String entityName, String errorCode) {
        super(HttpStatus.GONE, message, entityName, errorCode);
    }


    /**
     * Create a {@link RequestGoneException} with the given message, relatedEntityName, errorCode.
     *
     * @param message    the message.
     * @param entityName relatedEntityName from {@link EntityName}.
     * @param errorCode  errorCode from {@link ErrorConstants}
     * @param paramMap   map of additional information.
     */
    public RequestGoneException(String message, String entityName, String errorCode,
            Map<String, String> paramMap) {
        super(HttpStatus.GONE, message, entityName, errorCode, paramMap);
    }
}
