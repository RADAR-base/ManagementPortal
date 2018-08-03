package org.radarcns.management.web.rest.errors;

import java.util.Map;
import javax.ws.rs.core.Response;

/**
 * Created by dverbeec on 7/09/2017.
 * Modified by nivethika on 2/08/2018
 */
public class NotFoundException extends RadarWebApplicationException {


    /**
     * Create a custom conflict exception with the given message, parameter map, and url to the
     * conflicting resource.
     * @param message the message
     */
    public NotFoundException(String message, String entityName, String errorCode) {
        super(Response.Status.NOT_FOUND, message, entityName, errorCode);
    }

    /**
     * Create a custom conflict exception with the given message, parameter map, and url to the
     * conflicting resource.
     * @param message the message
     * @param paramMap the parameter map
     */
    public NotFoundException(String message, String entityName, String errorCode,
        Map<String, String> paramMap) {
        super(Response.Status.NOT_FOUND, message, entityName, errorCode, paramMap);
    }
}
