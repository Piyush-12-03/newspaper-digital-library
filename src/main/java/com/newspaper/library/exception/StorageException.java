package com.newspaper.library.exception;

/**
 * Base exception for storage-related errors.
 * Abstracts away AWS-specific exceptions.
 */
public class StorageException extends RuntimeException {

  public StorageException(String message) {
    super(message);
  }

  public StorageException(String message, Throwable cause) {
    super(message, cause);
  }
}
