package com.newspaper.library.exception;

/**
 * Exception thrown when an invalid file is uploaded or processed.
 */
public class InvalidFileException extends RuntimeException {

  public InvalidFileException(String message) {
    super(message);
  }

  public InvalidFileException(String message, Throwable cause) {
    super(message, cause);
  }
}
