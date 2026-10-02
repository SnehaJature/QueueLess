package com.queueless.queueservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(build(404, "NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(BadRequestException ex) {
        return ResponseEntity.badRequest().body(build(400, "BAD_REQUEST", ex.getMessage()));
    }

    @ExceptionHandler(QueueClosedException.class)
    public ResponseEntity<ErrorResponse> handleQueueClosed(QueueClosedException ex) {
        return ResponseEntity.badRequest().body(build(400, "QUEUE_CLOSED", ex.getMessage()));
    }

    @ExceptionHandler(AlreadyJoinedQueueException.class)
    public ResponseEntity<ErrorResponse> handleAlreadyJoined(AlreadyJoinedQueueException ex) {
        return ResponseEntity.badRequest().body(build(400, "ALREADY_IN_QUEUE", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(e -> errors.put(((FieldError) e).getField(), e.getDefaultMessage()));
        return ResponseEntity.badRequest().body(ErrorResponse.builder()
                .timestamp(LocalDateTime.now()).status(400)
                .error("VALIDATION_FAILED").message("Validation failed")
                .validationErrors(errors).build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(build(500, "INTERNAL_ERROR", "An unexpected error occurred"));
    }

    private ErrorResponse build(int status, String error, String message) {
        return ErrorResponse.builder().timestamp(LocalDateTime.now())
                .status(status).error(error).message(message).build();
    }
}
