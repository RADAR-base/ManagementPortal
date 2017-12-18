package org.radarcns.management.web.rest.errors;

/**
 * Exception to be thrown when the user requests an illegal operation to be performed.
 */
public class IllegalOperationException extends RuntimeException {
    public IllegalOperationException(String message) {
        super(message);
    }
}
