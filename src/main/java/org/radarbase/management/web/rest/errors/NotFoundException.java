package org.radarbase.management.web.rest.errors;

import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * Created by dverbeec on 7/09/2017.
 * Modified by nivethika on 2/08/2018.
 * <p>The server has not found anything matching the Request-URI.
 * No indication is given of whether the condition is temporary or permanent.
 * </p>
 */
public class NotFoundException extends RadarWebApplicationException {

    /**
     * Create a {@link NotFoundException} with the given message, relatedEntityName, errorCode.
     *
     * @param message    the message.
     * @param entityName relatedEntityName from {@link EntityName}.
     * @param errorCode  errorCode from {@link ErrorConstants}
     */
    public NotFoundException(String message, String entityName, String errorCode) {
        super(HttpStatus.NOT_FOUND, message, entityName, errorCode);
    }

    /**
     * Create a {@link NotFoundException} with the given message, relatedEntityName, errorCode.
     *
     * @param message    the message.
     * @param entityName relatedEntityName from {@link EntityName}.
     * @param errorCode  errorCode from {@link ErrorConstants}
     * @param paramMap   map of additional information.
     */
    public NotFoundException(String message, String entityName, String errorCode,
            Map<String, String> paramMap) {
        super(HttpStatus.NOT_FOUND, message, entityName, errorCode, paramMap);
    }
}
