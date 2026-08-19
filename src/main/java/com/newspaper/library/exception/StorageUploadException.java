package com.newspaper.library.exception;

/**
 * Exception thrown when file upload fails.
 */
public class StorageUploadException extends StorageException {

  public StorageUploadException(String message) {
    super(message);
  }

  public StorageUploadException(String message, Throwable cause) {
    super(message, cause);
  }
}
