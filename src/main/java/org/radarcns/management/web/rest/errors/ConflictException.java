package org.radarcns.management.web.rest.errors;

import java.util.Map;
import javax.ws.rs.core.Response;


/**
 * Created by dverbeec on 13/09/2017.
 * <p>
 * The request could not be completed due to a conflict with the current state of the resource.
 * This code is only allowed in situations where it is expected that the user might be able to
 * resolve the conflict and resubmit the request. The response body SHOULD include enough
 * information for the user to recognize the source of the conflict. Ideally, the response
 * entity would include enough information for the user or user agent to fix the problem;
 * however, that might not be possible and is not required.
 * </p>
 */
public class ConflictException extends RadarWebApplicationException {

    /**
     * Create a {@link ConflictException} with the given message, relatedEntityName, errorCode.
     *
     * @param message    the message.
     * @param entityName relatedEntityName from {@link EntityName}.
     * @param errorCode  errorCode from {@link ErrorConstants}
     */
    public ConflictException(String message, String entityName, String errorCode) {
        super(Response.Status.CONFLICT, message, entityName, errorCode);
    }

    /**
     * Create a {@link ConflictException} with the given message, relatedEntityName, errorCode.
     *
     * @param message    the message.
     * @param entityName relatedEntityName from {@link EntityName}.
     * @param errorCode  errorCode from {@link ErrorConstants}
     * @param paramMap   map of additional information.
     */
    public ConflictException(String message, String entityName, String errorCode,
            Map<String, String> paramMap) {
        super(Response.Status.CONFLICT, message, entityName, errorCode, paramMap);
    }
}
