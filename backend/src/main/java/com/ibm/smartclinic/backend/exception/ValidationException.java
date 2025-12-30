package com.ibm.smartclinic.backend.exception;

/**
 * Exception thrown when request validation fails.
 * Used for invalid input data or missing required fields.
 * HTTP Status: 400 Bad Request
 */
public class ValidationException extends RuntimeException {
    private String field;

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, String field) {
        super(message);
        this.field = field;
    }

    public ValidationException(String message, String field, Throwable cause) {
        super(message, cause);
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
