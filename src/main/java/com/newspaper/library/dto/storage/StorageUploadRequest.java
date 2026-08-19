package com.newspaper.library.dto.storage;

import lombok.Builder;
import lombok.Value;

import java.io.InputStream;

/**
 * Request object for uploading files to storage.
 * Contains only storage-layer concerns, no business logic.
 */
@Value
@Builder
public class StorageUploadRequest {

  /**
   * File content as stream
   */
  InputStream inputStream;

  /**
   * Content type (e.g., application/pdf)
   */
  String contentType;

  /**
   * File size in bytes
   */
  Long fileSize;

  /**
   * Storage key (path where file will be stored)
   * Example: newspaper/2026/08/15/bhopal-city/issue.pdf
   */
  String storageKey;
}
