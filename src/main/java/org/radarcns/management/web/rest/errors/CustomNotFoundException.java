package org.radarcns.management.web.rest.errors;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by dverbeec on 7/09/2017.
 */
public class CustomNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String message;

    private final Map<String, String> paramMap = new HashMap<>();

    /**
     * Create a not found exception with the given message and parameter map.
     * @param message the message
     * @param paramMap the parameter map
     */
    public CustomNotFoundException(String message, Map<String, String> paramMap) {
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
