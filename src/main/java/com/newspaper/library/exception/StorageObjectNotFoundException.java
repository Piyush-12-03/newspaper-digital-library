package com.newspaper.library.exception;

/**
 * Exception thrown when a storage object is not found.
 */
public class StorageObjectNotFoundException extends StorageException {

  public StorageObjectNotFoundException(String storageKey) {
    super("Storage object not found: " + storageKey);
  }

  public StorageObjectNotFoundException(String storageKey, Throwable cause) {
    super("Storage object not found: " + storageKey, cause);
  }
}
