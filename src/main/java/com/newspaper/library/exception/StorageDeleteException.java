package com.newspaper.library.exception;

/**
 * Exception thrown when file deletion fails.
 */
public class StorageDeleteException extends StorageException {

  public StorageDeleteException(String message) {
    super(message);
  }

  public StorageDeleteException(String message, Throwable cause) {
    super(message, cause);
  }
}
