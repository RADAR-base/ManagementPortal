package org.radarcns.management.web.rest.errors;

import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Created by dverbeec on 13/09/2017.
 */
public class CustomConflictException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String message;
    private final URI conflictingResource;
    private final Map<String, String> paramMap = new HashMap<>();

    /**
     * Create a custom conflict exception with the given message, parameter map, and url to the
     * conflicting resource.
     * @param message the message
     * @param paramMap the parameter map
     * @param conflictingResource the conflicting resource
     */
    public CustomConflictException(String message, Map<String, String> paramMap,
            URI conflictingResource) {
        super(message);
        Objects.requireNonNull(message, "message can not be null");
        Objects.requireNonNull(paramMap, "paramMap can not be null");
        Objects.requireNonNull(conflictingResource, "conflictingResource can not be null");
        this.message = message;
        this.conflictingResource = conflictingResource;
        // add default timestamp first, so a timestamp key in the paramMap will overwrite it
        this.paramMap.put("timestamp", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                .format(new Date()));
        this.paramMap.put("conflict", conflictingResource.toString());
        this.paramMap.putAll(paramMap);
    }

    public URI getConflictingResource() {
        return conflictingResource;
    }

    public ParameterizedErrorVM getErrorVM() {
        return new ParameterizedErrorVM(message, paramMap);
    }
}
