package org.radarbase.management.web.rest.errors;

import java.util.Map;
import javax.ws.rs.core.Response;

/**
 * The request could not be understood by the server due to malformed syntax.
 * The client SHOULD NOT repeat the request without modifications.
 * (e.g. malformed request syntax, size too large, invalid request message framing,
 * or deceptive request routing).
 */
public class BadRequestException extends RadarWebApplicationException {

    /**
     * Create a BadRequestException with the given message, relatedEntityName, errorCode.
     *
     * @param message    the message.
     * @param entityName relatedEntityName from {@link EntityName}.
     * @param errorCode  errorCode from {@link ErrorConstants}
     */
    public BadRequestException(String message, String entityName, String errorCode) {
        super(Response.Status.BAD_REQUEST, message, entityName, errorCode);
    }

    /**
     * Create a BadRequestException with the given message, relatedEntityName, errorCode.
     *
     * @param message    the message.
     * @param entityName relatedEntityName from {@link EntityName}.
     * @param errorCode  errorCode from {@link ErrorConstants}
     * @param paramMap   map of additional information.
     */
    public BadRequestException(String message, String entityName, String errorCode,
            Map<String, String> paramMap) {
        super(Response.Status.BAD_REQUEST, message, entityName, errorCode, paramMap);
    }
}
