package com.ibm.smartclinic.backend.exception;

/**
 * Exception thrown when a business logic violation or conflict occurs.
 * Examples: double-booking appointment, duplicate email, invalid state transition.
 * HTTP Status: 409 Conflict
 */
public class ConflictException extends RuntimeException {
    private String code;

    public ConflictException(String message) {
        super(message);
    }

    public ConflictException(String message, String code) {
        super(message);
        this.code = code;
    }

    public ConflictException(String message, Throwable cause) {
        super(message, cause);
    }

    public String getCode() {
        return code;
    }
}
