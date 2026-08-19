package com.newspaper.library.service.impl;

import com.newspaper.library.config.properties.S3Properties;
import com.newspaper.library.dto.storage.StorageUploadRequest;
import com.newspaper.library.dto.storage.StorageUploadResult;
import com.newspaper.library.exception.*;
import com.newspaper.library.service.PdfStorageService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.InputStream;
import java.time.Duration;

/**
 * AWS S3 implementation of PdfStorageService.
 * <p>
 * - Uses streaming upload/download (no full file in memory)
 * - Translates AWS exceptions to application exceptions
 * - Uses SDK's built-in retry mechanism
 * - Records metrics for observability
 */
@Slf4j
@Primary
@Service
@RequiredArgsConstructor
public class S3FileStorageServiceImpl implements PdfStorageService {

  private final S3Client s3Client;
  private final S3Properties s3Properties;
  private final MeterRegistry meterRegistry;

  @Override
  public StorageUploadResult upload(StorageUploadRequest request) {
    long startTime = System.currentTimeMillis();
    String storageKey = request.getStorageKey();

    try {
      log.debug("Uploading file - key: {}, size: {}, contentType: {}",
              storageKey, request.getFileSize(), request.getContentType());

      PutObjectRequest putRequest = PutObjectRequest.builder()
              .bucket(s3Properties.getBucket())
              .key(storageKey)
              .contentType(request.getContentType())
              .contentLength(request.getFileSize())
              .contentDisposition("inline; filename=\"issue.pdf\"")
              .build();

      RequestBody requestBody = RequestBody.fromInputStream(
              request.getInputStream(),
              request.getFileSize());

      PutObjectResponse response = s3Client.putObject(putRequest, requestBody);

      long duration = System.currentTimeMillis() - startTime;
      log.info("Upload successful - key: {}, size: {}, duration: {}ms, etag: {}",
              storageKey, request.getFileSize(), duration, response.eTag());

      recordMetric("s3.upload.success", true);
      recordTimer("s3.upload.duration", duration);

      return StorageUploadResult.builder()
              .storageKey(storageKey)
              .etag(response.eTag())
              .fileSize(request.getFileSize())
              .contentType(request.getContentType())
              .build();

    } catch (S3Exception e) {
      recordMetric("s3.upload.failure", true);
      String errorMsg = e.awsErrorDetails() != null ? e.awsErrorDetails().errorMessage() : e.getMessage();
      log.error("S3 upload failed - key: {}, error: {}", storageKey, errorMsg, e);
      throw new StorageUploadException("Failed to upload file to storage", e);

    } catch (Exception e) {
      recordMetric("s3.upload.failure", true);
      log.error("Unexpected error during upload - key: {}", storageKey, e);
      throw new StorageUploadException("Unexpected error during file upload", e);
    }
  }

  @Override
  public void delete(String storageKey) {
    long startTime = System.currentTimeMillis();

    try {
      log.debug("Deleting file - key: {}", storageKey);

      DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
              .bucket(s3Properties.getBucket())
              .key(storageKey)
              .build();

      s3Client.deleteObject(deleteRequest);

      long duration = System.currentTimeMillis() - startTime;
      log.info("Delete successful - key: {}, duration: {}ms", storageKey, duration);

      recordMetric("s3.delete.success", true);
      recordTimer("s3.delete.duration", duration);

    } catch (S3Exception e) {
      recordMetric("s3.delete.failure", true);
      String errorMsg = e.awsErrorDetails() != null ? e.awsErrorDetails().errorMessage() : e.getMessage();
      log.error("S3 delete failed - key: {}, error: {}", storageKey, errorMsg, e);
      throw new StorageDeleteException("Failed to delete file from storage", e);

    } catch (Exception e) {
      recordMetric("s3.delete.failure", true);
      log.error("Unexpected error during delete - key: {}", storageKey, e);
      throw new StorageDeleteException("Unexpected error during file deletion", e);
    }
  }

  @Override
  public boolean exists(String storageKey) {
    try {
      log.debug("Checking file existence - key: {}", storageKey);

      HeadObjectRequest headRequest = HeadObjectRequest.builder()
              .bucket(s3Properties.getBucket())
              .key(storageKey)
              .build();

      s3Client.headObject(headRequest);
      log.debug("File exists - key: {}", storageKey);
      return true;

    } catch (NoSuchKeyException e) {
      log.debug("File does not exist - key: {}", storageKey);
      return false;

    } catch (S3Exception e) {
      log.warn("Error checking file existence - key: {}, assuming does not exist", storageKey, e);
      return false;
    }
  }

  @Override
  public String generateDownloadUrl(String storageKey, Duration expiration) {
    try {
      log.debug("Generating presigned URL - key: {}, expiration: {}", storageKey, expiration);

      try (S3Presigner presigner = S3Presigner.builder()
              .region(software.amazon.awssdk.regions.Region.of(s3Properties.getRegion()))
              .build()) {

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(s3Properties.getBucket())
                .key(storageKey)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(expiration)
                .getObjectRequest(getObjectRequest)
                .build();

        PresignedGetObjectRequest presignedRequest = presigner.presignGetObject(presignRequest);
        String url = presignedRequest.url().toString();

        log.info("Presigned URL generated - key: {}, expiration: {}", storageKey, expiration);
        return url;
      }

    } catch (S3Exception e) {
      log.error("S3 error generating presigned URL - key: {}", storageKey, e);
      throw new StorageException("Failed to generate download URL", e);

    } catch (Exception e) {
      log.error("Unexpected error generating presigned URL - key: {}", storageKey, e);
      throw new StorageException("Unexpected error generating download URL", e);
    }
  }

  @Override
  public String generatePresignedDownloadUrl(String storageKey, String downloadFilename) {
    Duration defaultExpiration = Duration.ofMinutes(10);

    try {
      log.debug("Generating presigned download URL with filename - key: {}, filename: {}, expiration: {}",
              storageKey, downloadFilename, defaultExpiration);

      try (S3Presigner presigner = S3Presigner.builder()
              .region(software.amazon.awssdk.regions.Region.of(s3Properties.getRegion()))
              .build()) {

        // Set Content-Disposition to attachment with the desired filename
        String contentDisposition = String.format("attachment; filename=\"%s\"", downloadFilename);

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(s3Properties.getBucket())
                .key(storageKey)
                .responseContentDisposition(contentDisposition)
                .responseContentType("application/pdf")
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(defaultExpiration)
                .getObjectRequest(getObjectRequest)
                .build();

        PresignedGetObjectRequest presignedRequest = presigner.presignGetObject(presignRequest);
        String url = presignedRequest.url().toString();

        log.info("Presigned download URL generated - key: {}, filename: {}, expiration: {}",
                storageKey, downloadFilename, defaultExpiration);
        return url;
      }

    } catch (S3Exception e) {
      log.error("S3 error generating presigned download URL - key: {}, filename: {}",
              storageKey, downloadFilename, e);
      throw new StorageException("Failed to generate presigned download URL", e);

    } catch (Exception e) {
      log.error("Unexpected error generating presigned download URL - key: {}, filename: {}",
              storageKey, downloadFilename, e);
      throw new StorageException("Unexpected error generating presigned download URL", e);
    }
  }

  private void recordMetric(String metricName, boolean value) {
    try {
      Counter.builder(metricName)
              .description("S3 storage operation metric")
              .register(meterRegistry)
              .increment();
    } catch (Exception e) {
      log.warn("Failed to record metric: {}", metricName, e);
    }
  }

  private void recordTimer(String metricName, long durationMs) {
    try {
      Timer.builder(metricName)
              .description("S3 storage operation duration")
              .register(meterRegistry)
              .record(Duration.ofMillis(durationMs));
    } catch (Exception e) {
      log.warn("Failed to record timer: {}", metricName, e);
    }
  }
}
