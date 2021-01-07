package org.radarcns.management.web.rest.errors;

import static java.util.Collections.emptyMap;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

/**
 * A base parameterized exception, which can be translated on the client side.
 *
 * <p>For example: </p>
 *
 * <p>{@code throw new RadarWebApplicationException("Message to client", "entity",
 * "error.errorCode")}</p>
 *
 * <p>can be translated with:</p>
 *
 * <p>{@code "error.myCustomError" : "The server says {{param0}} to {{param1}}"}</p>
 */
public abstract class RadarWebApplicationException extends WebApplicationException {

    private String message;

    private String entityName;

    private String errorCode;

    private final Map<String, String> paramMap = new HashMap<>();

    /**
     * Create an exception with the given parameters. This will be used to to create response
     * body of the request.
     *
     * @param message    Error message to the client
     * @param entityName Entity related to the exception
     * @param errorCode  error code defined in MP if relevant.
     */
    public RadarWebApplicationException(Status status, String message, String entityName,
            String errorCode) {
        this(status, message, entityName, errorCode, emptyMap());
    }


    /**
     * A base parameterized exception, which can be translated on the client side.
     * @param status {@link javax.ws.rs.core.Response.Status} code.
     * @param message message to client.
     * @param entityName entityRelated from {@link EntityName}
     * @param errorCode errorCode from {@link ErrorConstants}
     * @param params map of optional information.
     */
    public RadarWebApplicationException(Status status, String message, String entityName,
            String errorCode, Map<String, String> params) {
        super(status);
        // add default timestamp first, so a timestamp key in the paramMap will overwrite it
        this.paramMap.put("timestamp",
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
                        .format(new Date()));
        this.paramMap.putAll(params);
        this.message = message;
        this.entityName = entityName;
        this.errorCode = errorCode;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public String getEntityName() {
        return entityName;
    }

    public String getErrorCode() {
        return errorCode;
    }

    protected RadarWebApplicationExceptionVM getExceptionVM() {
        return new RadarWebApplicationExceptionVM(message, entityName, errorCode, paramMap);
    }
}
