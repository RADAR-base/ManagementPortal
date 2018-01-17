package org.radarcns.management.web.rest.errors;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Custom, parameterized exception, which can be translated on the client side.
 *
 * <p>For example: </p>
 *
 * <p>{@code throw new CustomParameterizedException("error.myCustomError", "hello", "world")}</p>
 *
 * <p>can be translated with:</p>
 *
 * <p>{@code "error.myCustomError" : "The server says {{param0}} to {{param1}}"}</p>
 */
public class CustomParameterizedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private static final String PARAM = "param";

    private final String message;

    private final Map<String, String> paramMap = new HashMap<>();

    /**
     * Create an error with the given message and parameters. The parameters passed in will go
     * into the parameter with as values, with keys being {@code param0, param1, ...}.
     * @param message the error message
     * @param params the error parameters
     */
    public CustomParameterizedException(String message, String... params) {
        super(message);
        this.message = message;
        // add default timestamp first, so a timestamp key in the paramMap will overwrite it
        this.paramMap.put("timestamp", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                .format(new Date()));
        if (params != null && params.length > 0) {
            for (int i = 0; i < params.length; i++) {
                paramMap.put(PARAM + i, params[i]);
            }
        }
    }

    /**
     * Create an error with the given message and parameter map.
     * @param message the error message
     * @param paramMap the parameter map
     */
    public CustomParameterizedException(String message, Map<String, String> paramMap) {
        super(message);
        this.message = message;
        // add default timestamp first, so a timestamp key in the paramMap will overwrite it
        this.paramMap.put("timestamp", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                .format(new Date()));
        this.paramMap.putAll(paramMap);
    }

    public ParameterizedErrorVM getErrorVM() {
        return new ParameterizedErrorVM(message, paramMap);
    }
}
