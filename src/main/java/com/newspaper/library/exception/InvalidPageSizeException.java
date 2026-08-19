package com.newspaper.library.exception;

/**
 * Exception thrown when page size validation fails.
 */
public class InvalidPageSizeException extends RuntimeException {

  public InvalidPageSizeException(String message) {
    super(message);
  }
}
