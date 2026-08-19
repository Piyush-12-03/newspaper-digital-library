package com.newspaper.library.dto.storage;

import lombok.Builder;
import lombok.Value;

/**
 * Result object returned after successful upload.
 * Contains only essential metadata, no AWS-specific types.
 */
@Value
@Builder
public class StorageUploadResult {

  /**
   * Storage key where file was uploaded
   */
  String storageKey;

  /**
   * ETag from storage system (used for integrity verification)
   */
  String etag;

  /**
   * File size in bytes
   */
  Long fileSize;

  /**
   * Content type
   */
  String contentType;
}
