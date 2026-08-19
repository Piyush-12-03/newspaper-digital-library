package com.newspaper.library.exception;

/**
 * Exception thrown when file download fails.
 */
public class StorageDownloadException extends StorageException {

  public StorageDownloadException(String message) {
    super(message);
  }

  public StorageDownloadException(String message, Throwable cause) {
    super(message, cause);
  }
}
