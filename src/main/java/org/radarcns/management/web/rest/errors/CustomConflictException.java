package org.radarcns.management.web.rest.errors;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by dverbeec on 13/09/2017.
 */
public class CustomConflictException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private final String message;

    private final Map<String, String> paramMap = new HashMap<>();

    public CustomConflictException(String message, Map<String, String> paramMap) {
        super(message);
        this.message = message;
        this.paramMap.putAll(paramMap);
        this.paramMap.put("timestamp", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
    }

    public ParameterizedErrorVM getErrorVM() {
        return new ParameterizedErrorVM(message, paramMap);
    }
}
