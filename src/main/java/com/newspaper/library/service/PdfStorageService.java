package com.newspaper.library.service;

import com.newspaper.library.dto.storage.StorageUploadRequest;
import com.newspaper.library.dto.storage.StorageUploadResult;

import java.io.InputStream;
import java.time.Duration;

/**
 * Storage abstraction for PDF file operations.
 * <p>
 * Implementations must not expose storage-specific types (S3Object, etc.)
 * to controllers, services, or domain logic.
 * <p>
 * PostgreSQL remains the source of truth for metadata.
 * Storage layer only handles binary file operations.
 */
public interface PdfStorageService {

  /**
   * Upload file to storage.
   *
   * @param request Upload request with stream, content type, size, and key
   * @return Upload result with storage key and metadata
   * @throws com.newspaper.library.exception.StorageUploadException if upload fails
   */
  StorageUploadResult upload(StorageUploadRequest request);

  /**
   * Delete file from storage.
   *
   * @param storageKey Storage key (path to file)
   * @throws com.newspaper.library.exception.StorageDeleteException if deletion fails
   */
  void delete(String storageKey);

  /**
   * Check if file exists in storage.
   *
   * @param storageKey Storage key (path to file)
   * @return true if file exists, false otherwise
   */
  boolean exists(String storageKey);

  /**
   * Generate presigned download URL.
   *
   * @param storageKey Storage key (path to file)
   * @param expiration URL validity duration
   * @return Presigned URL string
   * @throws com.newspaper.library.exception.StorageException if URL generation fails
   */
  String generateDownloadUrl(String storageKey, Duration expiration);

  /**
   * Generate presigned download URL with custom filename for download.
   * The filename will be set in Content-Disposition header.
   *
   * @param storageKey       Storage key (path to file)
   * @param downloadFilename Desired filename for download
   * @return Presigned URL string
   * @throws com.newspaper.library.exception.StorageException if URL generation fails
   */
  String generatePresignedDownloadUrl(String storageKey, String downloadFilename);
}
