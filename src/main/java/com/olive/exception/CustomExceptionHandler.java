package com.olive.exception;

import com.olive.dto.MessageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.AccessDeniedException;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class CustomExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(CustomExceptionHandler.class);

    /**
     * Handles validation exceptions (e.g., @Valid annotations failing).
     * Returns a 400 Bad Request with details about validation errors.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage()));
        logger.warn("Validation failed: {}", errors);
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles custom ResponseStatusException, which allows services to throw
     * exceptions with specific HTTP status codes and messages.
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<MessageResponse> handleResponseStatusException(ResponseStatusException ex) {
        logger.warn("ResponseStatusException caught: {} - {}", ex.getStatusCode(), ex.getReason());
        return new ResponseEntity<>(new MessageResponse(ex.getReason()), ex.getStatusCode());
    }

    /**
     * Handles AccessDeniedException, typically thrown by Spring Security
     * when a user tries to access a resource they are not authorized for.
     * Returns a 403 Forbidden.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<MessageResponse> handleAccessDeniedException(AccessDeniedException ex) {
        logger.warn("Access Denied: {}", ex.getMessage());
        return new ResponseEntity<>(new MessageResponse("Access Denied: " + ex.getMessage()), HttpStatus.FORBIDDEN);
    }

    /**
     * Handles generic RuntimeException.
     * Returns a 500 Internal Server Error.
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<MessageResponse> handleAllExceptions(RuntimeException ex, WebRequest request) {
        logger.error("An unexpected error occurred: {}", ex.getMessage(), ex);
        return new ResponseEntity<>(new MessageResponse("An unexpected error occurred: " + ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handles FileStorageException, for issues related to file storage.
     */
    @ExceptionHandler(FileStorageException.class)
    public ResponseEntity<MessageResponse> handleFileStorageException(FileStorageException ex) {
        logger.error("File storage error: {}", ex.getMessage(), ex);
        return new ResponseEntity<>(new MessageResponse(ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handles MyFileNotFoundException, for when a requested file is not found.
     */
    @ExceptionHandler(MyFileNotFoundException.class)
    public ResponseEntity<MessageResponse> handleMyFileNotFoundException(MyFileNotFoundException ex) {
        logger.warn("File not found error: {}", ex.getMessage());
        return new ResponseEntity<>(new MessageResponse(ex.getMessage()), HttpStatus.NOT_FOUND);
    }
}
