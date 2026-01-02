package com.ibm.smartclinic.backend.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Global exception handler for the application.
 * Handles all application exceptions and returns standardized error responses.
 * Applies to all controllers with @RestController or @Controller annotations.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

        private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles ResourceNotFoundException.
     * Returns 404 Not Found.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleResourceNotFound(
            ResourceNotFoundException ex,
            HttpServletRequest request) {

        log.warn("Resource not found: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), "RESOURCE_NOT_FOUND", request);
    }

    /**
     * Handles ConflictException.
     * Returns 409 Conflict.
     */
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiError> handleConflict(
            ConflictException ex,
            HttpServletRequest request) {

        String errorCode = ex.getCode() != null ? ex.getCode() : "CONFLICT";
        log.warn("Conflict detected: {}", errorCode);
        return buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage(), errorCode, request);
    }

    /**
     * Handles ValidationException.
     * Returns 400 Bad Request.
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiError> handleValidation(
            ValidationException ex,
            HttpServletRequest request) {

        log.debug("Validation failure: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), "VALIDATION_FAILED", request);
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

        ApiError apiError = createApiError(HttpStatus.BAD_REQUEST, "Validation failed", "VALIDATION_FAILED", request);
        apiError.setFieldErrors(fieldErrors);

        log.debug("Method argument validation failed for {} fields", fieldErrors.size());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
    }

    /**
     * Handles IllegalArgumentException.
     * Returns 400 Bad Request.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(
            IllegalArgumentException ex,
            HttpServletRequest request) {

        log.debug("Illegal argument: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), "INVALID_ARGUMENT", request);
    }

    /**
     * Handles IllegalStateException.
     * Returns 409 Conflict (invalid state transition).
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiError> handleIllegalState(
            IllegalStateException ex,
            HttpServletRequest request) {

                log.warn("Illegal state encountered: {}", ex.getMessage());
                return buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage(), "INVALID_STATE", request);
        }

        /**
         * Handles AccessDeniedException (authorization failures).
         * Returns 403 Forbidden.
         */
        @ExceptionHandler(AccessDeniedException.class)
        public ResponseEntity<ApiError> handleAccessDenied(
                        AccessDeniedException ex,
                        HttpServletRequest request) {

                log.warn("Access denied: {}", ex.getMessage());
                return buildErrorResponse(HttpStatus.FORBIDDEN, ex.getMessage(), "ACCESS_DENIED", request);
        }

        /**
         * Handles AuthenticationException (authentication failures).
         * Returns 401 Unauthorized.
         */
        @ExceptionHandler(AuthenticationException.class)
        public ResponseEntity<ApiError> handleAuthentication(
                        AuthenticationException ex,
                        HttpServletRequest request) {

                log.warn("Authentication failed: {}", ex.getMessage());
                return buildErrorResponse(HttpStatus.UNAUTHORIZED, ex.getMessage(), "AUTHENTICATION_FAILED", request);
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

                log.error("Unexpected error", ex);
                return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", "INTERNAL_SERVER_ERROR", request);
        }

        private ResponseEntity<ApiError> buildErrorResponse(HttpStatus status, String message, String errorCode, HttpServletRequest request) {
                ApiError apiError = createApiError(status, message, errorCode, request);
                return ResponseEntity.status(status).body(apiError);
        }

        private ApiError createApiError(HttpStatus status, String message, String errorCode, HttpServletRequest request) {
                ApiError apiError = new ApiError(status.value(), message, errorCode);
                apiError.setPath(request.getRequestURI());
                return apiError;
    }
}
