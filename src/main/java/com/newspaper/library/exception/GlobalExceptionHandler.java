package com.newspaper.library.exception;

import com.newspaper.library.dto.common.GenericResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Global exception handler - returns GenericResponse for ALL errors.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<GenericResponse<Object>> handleResourceNotFound(
          ResourceNotFoundException ex, HttpServletRequest request) {
    logError(request, "Resource not found", ex.getMessage());
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(GenericResponse.error(404, ex.getMessage()));
  }

  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<GenericResponse<Object>> handleAuthenticationException(
          AuthenticationException ex, HttpServletRequest request) {
    logError(request, "Authentication failed", ex.getMessage());
    
    String message = "Authentication failed. Please provide valid credentials.";
    if (ex instanceof BadCredentialsException) {
      message = "Invalid username or password";
    } else if (ex instanceof InsufficientAuthenticationException) {
      message = "Authentication required. Please login to access this resource.";
    }
    
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(GenericResponse.error(401, message));
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<GenericResponse<Object>> handleAccessDenied(
          AccessDeniedException ex, HttpServletRequest request) {
    logError(request, "Access denied", ex.getMessage());
    return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(GenericResponse.error(403, 
                    "Access denied. You do not have permission to access this resource. Required role: ADMIN"));
  }

  @ExceptionHandler(DuplicateResourceException.class)
  public ResponseEntity<GenericResponse<Object>> handleDuplicateResource(
          DuplicateResourceException ex, HttpServletRequest request) {
    logError(request, "Duplicate resource", ex.getMessage());
    return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(GenericResponse.error(409, ex.getMessage()));
  }

  @ExceptionHandler(InvalidRequestException.class)
  public ResponseEntity<GenericResponse<Object>> handleInvalidRequest(
          InvalidRequestException ex, HttpServletRequest request) {
    logError(request, "Invalid request", ex.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(GenericResponse.error(400, ex.getMessage()));
  }

  @ExceptionHandler(InvalidDateRangeException.class)
  public ResponseEntity<GenericResponse<Object>> handleInvalidDateRange(
          InvalidDateRangeException ex, HttpServletRequest request) {
    logError(request, "Invalid date range", ex.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(GenericResponse.error(400, ex.getMessage()));
  }

  @ExceptionHandler(InvalidPageSizeException.class)
  public ResponseEntity<GenericResponse<Object>> handleInvalidPageSize(
          InvalidPageSizeException ex, HttpServletRequest request) {
    logError(request, "Invalid page size", ex.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(GenericResponse.error(400, ex.getMessage()));
  }

  @ExceptionHandler(InvalidFileException.class)
  public ResponseEntity<GenericResponse<Object>> handleInvalidFile(
          InvalidFileException ex, HttpServletRequest request) {
    logError(request, "Invalid file", ex.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(GenericResponse.error(400, ex.getMessage()));
  }

  @ExceptionHandler(StorageObjectNotFoundException.class)
  public ResponseEntity<GenericResponse<Object>> handleStorageObjectNotFound(
          StorageObjectNotFoundException ex, HttpServletRequest request) {
    logError(request, "Storage object not found", ex.getMessage());
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(GenericResponse.error(404, "File not found in storage"));
  }

  @ExceptionHandler(StorageUploadException.class)
  public ResponseEntity<GenericResponse<Object>> handleStorageUpload(
          StorageUploadException ex, HttpServletRequest request) {
    String traceId = getOrGenerateTraceId(request);
    log.error("Storage upload error [traceId={}]: {}", traceId, ex.getMessage(), ex);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(GenericResponse.error(500, "Failed to upload file"));
  }

  @ExceptionHandler(StorageDownloadException.class)
  public ResponseEntity<GenericResponse<Object>> handleStorageDownload(
          StorageDownloadException ex, HttpServletRequest request) {
    String traceId = getOrGenerateTraceId(request);
    log.error("Storage download error [traceId={}]: {}", traceId, ex.getMessage(), ex);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(GenericResponse.error(500, "Failed to download file"));
  }

  @ExceptionHandler(StorageDeleteException.class)
  public ResponseEntity<GenericResponse<Object>> handleStorageDelete(
          StorageDeleteException ex, HttpServletRequest request) {
    String traceId = getOrGenerateTraceId(request);
    log.error("Storage delete error [traceId={}]: {}", traceId, ex.getMessage(), ex);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(GenericResponse.error(500, "Failed to delete file"));
  }

  @ExceptionHandler(StorageException.class)
  public ResponseEntity<GenericResponse<Object>> handleStorageException(
          StorageException ex, HttpServletRequest request) {
    String traceId = getOrGenerateTraceId(request);
    log.error("Storage error [traceId={}]: {}", traceId, ex.getMessage(), ex);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(GenericResponse.error(500, "Storage operation failed"));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<GenericResponse<Map<String, String>>> handleValidationErrors(
          MethodArgumentNotValidException ex, HttpServletRequest request) {
    String traceId = getOrGenerateTraceId(request);
    log.warn("Validation failed [traceId={}]", traceId);

    Map<String, String> errors = new HashMap<>();
    for (FieldError error : ex.getBindingResult().getFieldErrors()) {
      errors.put(error.getField(), error.getDefaultMessage());
    }

    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(GenericResponse.error(400, "Validation failed", errors));
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<GenericResponse<Map<String, String>>> handleConstraintViolation(
          ConstraintViolationException ex, HttpServletRequest request) {
    String traceId = getOrGenerateTraceId(request);
    log.warn("Constraint violation [traceId={}]: {}", traceId, ex.getMessage());

    Map<String, String> errors = new HashMap<>();
    ex.getConstraintViolations().forEach(violation -> {
      String fieldName = violation.getPropertyPath().toString();
      errors.put(fieldName, violation.getMessage());
    });

    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(GenericResponse.error(400, "Constraint violation", errors));
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<GenericResponse<Object>> handleTypeMismatch(
          MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
    String traceId = getOrGenerateTraceId(request);
    log.warn("Type mismatch [traceId={}]: parameter '{}' with value '{}'",
            traceId, ex.getName(), ex.getValue());

    String message = String.format("Invalid value '%s' for parameter '%s'",
            ex.getValue(), ex.getName());

    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(GenericResponse.error(400, message));
  }

  @ExceptionHandler(DateTimeParseException.class)
  public ResponseEntity<GenericResponse<Object>> handleDateTimeParse(
          DateTimeParseException ex, HttpServletRequest request) {
    logError(request, "Date parse error", ex.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(GenericResponse.error(400, "Invalid date format. Expected: yyyy-MM-dd"));
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<GenericResponse<Object>> handleMessageNotReadable(
          HttpMessageNotReadableException ex, HttpServletRequest request) {
    logError(request, "Message not readable", ex.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(GenericResponse.error(400, "Malformed request body or invalid JSON"));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<GenericResponse<Object>> handleGenericException(
          Exception ex, HttpServletRequest request) {
    String traceId = getOrGenerateTraceId(request);
    log.error("Unexpected error [traceId={}]", traceId, ex);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(GenericResponse.error(500, "An unexpected error occurred"));
  }

  private void logError(HttpServletRequest request, String type, String message) {
    String traceId = getOrGenerateTraceId(request);
    log.warn("{} [traceId={}]: {}", type, traceId, message);
  }

  private String getOrGenerateTraceId(HttpServletRequest request) {
    String traceId = request.getHeader("X-Request-ID");
    return (traceId == null || traceId.isBlank()) ? UUID.randomUUID().toString() : traceId;
  }
}
