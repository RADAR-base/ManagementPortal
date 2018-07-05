package org.radarcns.management.web.rest.errors;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Custom exception to throw runtime errors that will be translated to an error response with
 * status code 500. The parameter map can contain a key {@code message} whose value will be
 * displayed in the ManagementPortalApp.
 */
public class CustomServerException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private final String message;

    private final Map<String, String> paramMap = new HashMap<>();

    /**
     * Create a not found exception with the given message and parameter map.
     * @param message the message
     * @param paramMap the parameter map
     */
    public CustomServerException(String message, Map<String, String> paramMap) {
        super(message);
        this.message = message;
        this.paramMap.putAll(paramMap);
        this.paramMap
                .put("timestamp", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
    }

    public ParameterizedErrorVM getErrorVM() {
        return new ParameterizedErrorVM(message, paramMap);
    }
}
