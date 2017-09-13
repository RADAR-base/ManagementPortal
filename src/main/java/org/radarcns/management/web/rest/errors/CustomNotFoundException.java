package org.radarcns.management.web.rest.errors;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by dverbeec on 7/09/2017.
 */
public class CustomNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private final String message;

    private final Map<String, String> paramMap = new HashMap<>();

    public CustomNotFoundException(String message, Map<String, String> paramMap) {
        super(message);
        this.message = message;
        this.paramMap.putAll(paramMap);
    }

    public ParameterizedErrorVM getErrorVM() {
        return new ParameterizedErrorVM(message, paramMap);
    }
}
