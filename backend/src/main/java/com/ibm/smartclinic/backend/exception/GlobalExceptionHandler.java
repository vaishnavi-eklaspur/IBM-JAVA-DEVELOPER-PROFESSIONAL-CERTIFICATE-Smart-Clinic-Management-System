package com.ibm.smartclinic.backend.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * Global exception handler for the application.
 * Handles all application exceptions and returns standardized error responses.
 * Applies to all controllers with @RestController or @Controller annotations.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles ResourceNotFoundException.
     * Returns 404 Not Found.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleResourceNotFound(
            ResourceNotFoundException ex,
            HttpServletRequest request) {

        ApiError apiError = new ApiError(
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                "RESOURCE_NOT_FOUND"
        );
        apiError.setPath(request.getRequestURI());

        return new ResponseEntity<>(apiError, HttpStatus.NOT_FOUND);
    }

    /**
     * Handles ConflictException.
     * Returns 409 Conflict.
     */
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiError> handleConflict(
            ConflictException ex,
            HttpServletRequest request) {

        ApiError apiError = new ApiError(
                HttpStatus.CONFLICT.value(),
                ex.getMessage(),
                ex.getCode() != null ? ex.getCode() : "CONFLICT"
        );
        apiError.setPath(request.getRequestURI());

        return new ResponseEntity<>(apiError, HttpStatus.CONFLICT);
    }

    /**
     * Handles ValidationException.
     * Returns 400 Bad Request.
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiError> handleValidation(
            ValidationException ex,
            HttpServletRequest request) {

        ApiError apiError = new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                "VALIDATION_FAILED"
        );
        apiError.setPath(request.getRequestURI());

        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles Spring validation errors (MethodArgumentNotValidException).
     * Returns 400 Bad Request with detailed field errors.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        List<ApiError.FieldError> fieldErrors = new ArrayList<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            Object rejectedValue = ((FieldError) error).getRejectedValue();
            fieldErrors.add(new ApiError.FieldError(fieldName, errorMessage, rejectedValue));
        });

        ApiError apiError = new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                "Validation failed",
                "VALIDATION_FAILED"
        );
        apiError.setFieldErrors(fieldErrors);
        apiError.setPath(request.getRequestURI());

        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles IllegalArgumentException.
     * Returns 400 Bad Request.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(
            IllegalArgumentException ex,
            HttpServletRequest request) {

        ApiError apiError = new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                "INVALID_ARGUMENT"
        );
        apiError.setPath(request.getRequestURI());

        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles IllegalStateException.
     * Returns 409 Conflict (invalid state transition).
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiError> handleIllegalState(
            IllegalStateException ex,
            HttpServletRequest request) {

        ApiError apiError = new ApiError(
                HttpStatus.CONFLICT.value(),
                ex.getMessage(),
                "INVALID_STATE"
        );
        apiError.setPath(request.getRequestURI());

        return new ResponseEntity<>(apiError, HttpStatus.CONFLICT);
    }

    /**
     * Handles generic Exception.
     * Returns 500 Internal Server Error.
     * Logs detailed error information for debugging.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGenericException(
            Exception ex,
            HttpServletRequest request) {

        ApiError apiError = new ApiError(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "An unexpected error occurred",
                "INTERNAL_SERVER_ERROR"
        );
        apiError.setPath(request.getRequestURI());

        // Log the full exception for debugging (in production, use proper logging)
        ex.printStackTrace();

        return new ResponseEntity<>(apiError, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
