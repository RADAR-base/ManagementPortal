package org.radarcns.management.web.rest.errors;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * View Model for sending a {@link RadarWebApplicationException}
 */
public class RadarWebApplicationExceptionVM implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty
    private final String entityName;

    @JsonProperty
    private final String errorCode;

    @JsonProperty
    private final String message;

    @JsonProperty
    private Map<String, String> params;

    /**
     * Creates an error view model with message, entityName and errorCode.
     * @param message for the client.
     * @param entityName related entity.
     * @param errorCode errorCode.
     */
    public RadarWebApplicationExceptionVM(String message, String entityName, String errorCode) {
        this(message, entityName, errorCode, Collections.emptyMap());
    }

    public RadarWebApplicationExceptionVM(String message, String entityName, String errorCode, Map<String,
            String> params) {
        this.message = message;
        this.entityName = entityName;
        this.errorCode = errorCode;
        this.params = params;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public String getEntityName() {
        return entityName;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RadarWebApplicationExceptionVM that = (RadarWebApplicationExceptionVM) o;
        return Objects.equals(entityName, that.entityName) && Objects
            .equals(errorCode, that.errorCode) && Objects.equals(message, that.message) && Objects
            .equals(params, that.params);
    }

    @Override
    public int hashCode() {

        return Objects.hash(entityName, errorCode, message, params);
    }

    @Override
    public String toString() {
        return "RadarWebApplicationExceptionVM{" + "entityName='" + entityName + '\'' + ", errorCode='"
            + errorCode + '\'' + ", message='" + message + '\'' + ", params=" + params + '}';
    }
}
