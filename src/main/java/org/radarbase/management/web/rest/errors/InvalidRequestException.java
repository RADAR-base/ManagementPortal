package org.radarbase.management.web.rest.errors;

import java.util.Map;
import javax.ws.rs.core.Response;

/**
 * The server understood the request, but is refusing to fulfill it.
 * Authorization will not help and the request SHOULD NOT be repeated.
 */
public class InvalidRequestException extends RadarWebApplicationException {

    /**
     * Create a {@link InvalidRequestException} with the given message, relatedEntityName,errorCode.
     *
     * @param message    the message.
     * @param entityName relatedEntityName from {@link EntityName}.
     * @param errorCode  errorCode from {@link ErrorConstants}
     */
    public InvalidRequestException(String message, String entityName, String errorCode) {
        super(Response.Status.FORBIDDEN, message, entityName, errorCode);
    }

    /**
     * Create a {@link InvalidRequestException} with the given message, relatedEntityName,errorCode.
     *
     * @param message    the message.
     * @param entityName relatedEntityName from {@link EntityName}.
     * @param errorCode  errorCode from {@link ErrorConstants}
     * @param params     map of additional information.
     */
    public InvalidRequestException(String message, String entityName, String errorCode,
            Map<String, String> params) {
        super(Response.Status.FORBIDDEN, message, entityName, errorCode, params);
    }

}
