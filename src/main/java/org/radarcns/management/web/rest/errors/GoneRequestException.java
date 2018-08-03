package org.radarcns.management.web.rest.errors;

import java.util.Map;
import javax.ws.rs.core.Response;

/**
 * Throw when the requested resource is no longer available at the server and no forwarding
 * address is known. This condition is expected to be considered permanent. Clients with
 * link editing capabilities SHOULD delete references to the Request-URI after user approval.
 */
public class GoneRequestException extends RadarWebApplicationException {

    /**
     * Create a {@link GoneRequestException} with the given message, relatedEntityName, errorCode.
     *
     * @param message    the message.
     * @param entityName relatedEntityName from {@link EntityName}.
     * @param errorCode  errorCode from {@link ErrorConstants}
     */
    public GoneRequestException(String message, String entityName, String errorCode) {
        super(Response.Status.GONE, message, entityName, errorCode);
    }


    /**
     * Create a {@link GoneRequestException} with the given message, relatedEntityName, errorCode.
     *
     * @param message    the message.
     * @param entityName relatedEntityName from {@link EntityName}.
     * @param errorCode  errorCode from {@link ErrorConstants}
     * @param paramMap   map of additional information.
     */
    public GoneRequestException(String message, String entityName, String errorCode,
            Map<String, String> paramMap) {
        super(Response.Status.GONE, message, entityName, errorCode, paramMap);
    }
}
