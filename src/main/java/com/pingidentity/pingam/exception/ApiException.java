package com.pingidentity.pingam.exception;

/**
 * Exception thrown for API-related errors
 */
public class ApiException extends Exception {
    public ApiException(String message) {
        super(message);
    }

    public ApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
