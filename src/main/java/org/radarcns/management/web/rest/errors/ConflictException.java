package org.radarcns.management.web.rest.errors;

import java.util.Map;

import javax.ws.rs.core.Response;


/**
 * Created by dverbeec on 13/09/2017.
 */
public class ConflictException extends RadarWebApplicationException {

    /**
     * Create a custom conflict exception with the given message, parameter map, and url to the
     * conflicting resource.
     * @param message the message
     */
    public ConflictException(String message, String entityName, String errorCode) {
        super(Response.Status.CONFLICT, message, entityName, errorCode);
    }

    /**
     * Create a custom conflict exception with the given message, parameter map, and url to the
     * conflicting resource.
     * @param message the message
     * @param paramMap the parameter map
     */
    public ConflictException(String message, String entityName, String errorCode,
        Map<String, String> paramMap) {
        super(Response.Status.CONFLICT, message, entityName, errorCode, paramMap);
    }
}
