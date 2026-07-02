package com.wifiadmin.exception;

import com.wifiadmin.dto.ErrorBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorBody> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("Validation failed");
        log.warn("Validation error: {}", message);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorBody(message, "VALIDATION_ERROR"));
    }

    @ExceptionHandler(CpeNotFoundException.class)
    public ResponseEntity<ErrorBody> handleCpeNotFound(CpeNotFoundException ex, WebRequest request) {
        log.warn("CPE not found: {}", ex.getCpeId());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorBody(ex.getMessage(), "CPE_NOT_FOUND"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorBody> handleIllegalArgument(IllegalArgumentException ex, WebRequest request) {
        log.warn("Validation error: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorBody(ex.getMessage(), "VALIDATION_ERROR"));
    }

    @ExceptionHandler(PlatformCommunicationException.class)
    public ResponseEntity<ErrorBody> handlePlatformCommunication(PlatformCommunicationException ex, WebRequest request) {
        log.error("Platform communication error: {}", ex.getMessage(), ex);
        return ResponseEntity
                .status(HttpStatus.BAD_GATEWAY)
                .body(new ErrorBody("Failed to communicate with platform: " + ex.getMessage(), "PLATFORM_ERROR"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorBody> handleGeneral(Exception ex, WebRequest request) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorBody("Internal server error", "INTERNAL_ERROR"));
    }
}
