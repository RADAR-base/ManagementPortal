package org.radarcns.management.web.rest.errors;


import java.util.Map;
import javax.ws.rs.core.Response;

/**
 * The server encountered an unexpected condition which prevented it from fulfilling the request.
 */
public class InvalidStateException extends RadarWebApplicationException {

    /**
     * Create a {@link InvalidStateException} with the given message, relatedEntityName, errorCode.
     *
     * @param message    the message.
     * @param entityName relatedEntityName from {@link EntityName}.
     * @param errorCode  errorCode from {@link ErrorConstants}
     */
    public InvalidStateException(String message, String entityName, String errorCode) {
        super(Response.Status.INTERNAL_SERVER_ERROR, message, entityName, errorCode);
    }

    /**
     * Create a {@link InvalidStateException} with the given message, relatedEntityName, errorCode.
     *
     * @param message    the message.
     * @param entityName relatedEntityName from {@link EntityName}.
     * @param errorCode  errorCode from {@link ErrorConstants}
     * @param params     map of additional information.
     */
    public InvalidStateException(String message, String entityName, String errorCode,
            Map<String, String> params) {
        super(Response.Status.INTERNAL_SERVER_ERROR, message, entityName, errorCode, params);
    }
}
