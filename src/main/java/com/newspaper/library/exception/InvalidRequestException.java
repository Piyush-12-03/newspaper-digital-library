package com.newspaper.library.exception;

/**
 * Exception thrown when request contains invalid data or violates business rules.
 */
public class InvalidRequestException extends RuntimeException {
  public InvalidRequestException(String message) {
    super(message);
  }

  public InvalidRequestException(String message, Throwable cause) {
    super(message, cause);
  }
}
