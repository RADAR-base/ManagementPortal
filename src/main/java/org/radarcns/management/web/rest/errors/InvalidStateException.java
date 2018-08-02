package org.radarcns.management.web.rest.errors;


import java.util.Map;
import javax.ws.rs.core.Response;

public class InvalidStateException extends RadarWebApplicationException {

    public InvalidStateException(String message, String entityName,
        String errorCode) {
        super(Response.Status.INTERNAL_SERVER_ERROR, message, entityName, errorCode);
    }

    public InvalidStateException(String message, String entityName,
        String errorCode, Map<String, String> params) {
        super(Response.Status.INTERNAL_SERVER_ERROR, message, entityName, errorCode, params);
    }
}
