package com.newspaper.library.service.impl;

import com.newspaper.library.config.properties.S3Properties;
import com.newspaper.library.dto.storage.StorageUploadRequest;
import com.newspaper.library.dto.storage.StorageUploadResult;
import com.newspaper.library.exception.*;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for S3FileStorageServiceImpl.
 * Mocks AWS SDK to avoid real S3 calls.
 */
@ExtendWith(MockitoExtension.class)
class S3FileStorageServiceImplTest {

  @Mock
  private S3Client s3Client;

  private S3Properties s3Properties;
  private MeterRegistry meterRegistry;
  private S3FileStorageServiceImpl storageService;

  @BeforeEach
  void setUp() {
    s3Properties = new S3Properties();
    s3Properties.setBucket("test-bucket");
    s3Properties.setRegion("us-east-1");
    s3Properties.setMaxFileSizeMb(50);
    s3Properties.setDownloadUrlExpirationMinutes(10);
    s3Properties.setConnectionTimeoutMs(5000);
    s3Properties.setApiCallTimeoutMs(30000);

    meterRegistry = new SimpleMeterRegistry();
    storageService = new S3FileStorageServiceImpl(s3Client, s3Properties, meterRegistry);
  }

  @Test
  void upload_withValidRequest_shouldReturnUploadResult() {
    // Arrange
    String storageKey = "newspaper/2026/08/15/bhopal-city/issue.pdf";
    byte[] content = "test pdf content".getBytes();
    InputStream inputStream = new ByteArrayInputStream(content);

    StorageUploadRequest request = StorageUploadRequest.builder()
            .inputStream(inputStream)
            .contentType("application/pdf")
            .fileSize((long) content.length)
            .storageKey(storageKey)
            .build();

    PutObjectResponse mockResponse = PutObjectResponse.builder()
            .eTag("mock-etag-123")
            .build();

    when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
            .thenReturn(mockResponse);

    // Act
    StorageUploadResult result = storageService.upload(request);

    // Assert
    assertNotNull(result);
    assertEquals(storageKey, result.getStorageKey());
    assertEquals("mock-etag-123", result.getEtag());
    assertEquals(content.length, result.getFileSize());
    assertEquals("application/pdf", result.getContentType());

    verify(s3Client, times(1)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
  }

  @Test
  void upload_withS3Exception_shouldThrowStorageUploadException() {
    // Arrange
    String storageKey = "newspaper/2026/08/15/bhopal-city/issue.pdf";
    byte[] content = "test pdf content".getBytes();
    InputStream inputStream = new ByteArrayInputStream(content);

    StorageUploadRequest request = StorageUploadRequest.builder()
            .inputStream(inputStream)
            .contentType("application/pdf")
            .fileSize((long) content.length)
            .storageKey(storageKey)
            .build();

    S3Exception s3Exception = (S3Exception) S3Exception.builder()
            .message("S3 error")
            .statusCode(500)
            .build();

    when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
            .thenThrow(s3Exception);

    // Act & Assert
    assertThrows(StorageUploadException.class, () -> storageService.upload(request));
    verify(s3Client, times(1)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
  }

  @Test
  void upload_withGenericException_shouldThrowStorageUploadException() {
    // Arrange
    String storageKey = "newspaper/2026/08/15/bhopal-city/issue.pdf";
    byte[] content = "test pdf content".getBytes();
    InputStream inputStream = new ByteArrayInputStream(content);

    StorageUploadRequest request = StorageUploadRequest.builder()
            .inputStream(inputStream)
            .contentType("application/pdf")
            .fileSize((long) content.length)
            .storageKey(storageKey)
            .build();

    when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
            .thenThrow(new RuntimeException("Unexpected error"));

    // Act & Assert
    assertThrows(StorageUploadException.class, () -> storageService.upload(request));
    verify(s3Client, times(1)).putObject(any(PutObjectRequest.class), any(RequestBody.class));
  }

  @Test
  void delete_withExistingObject_shouldSucceed() {
    // Arrange
    String storageKey = "newspaper/2026/08/15/bhopal-city/issue.pdf";

    DeleteObjectResponse mockResponse = DeleteObjectResponse.builder().build();
    when(s3Client.deleteObject((DeleteObjectRequest) any()))
            .thenReturn(mockResponse);

    // Act
    assertDoesNotThrow(() -> storageService.delete(storageKey));

    // Assert
    verify(s3Client, times(1)).deleteObject((DeleteObjectRequest) any());
  }

  @Test
  void delete_withS3Exception_shouldThrowStorageDeleteException() {
    // Arrange
    String storageKey = "newspaper/2026/08/15/bhopal-city/issue.pdf";

    S3Exception s3Exception = (S3Exception) S3Exception.builder()
            .message("Delete failed")
            .statusCode(403)
            .build();

    when(s3Client.deleteObject((DeleteObjectRequest) any()))
            .thenThrow(s3Exception);

    // Act & Assert
    assertThrows(StorageDeleteException.class, () -> storageService.delete(storageKey));
    verify(s3Client, times(1)).deleteObject((DeleteObjectRequest) any());
  }

  @Test
  void exists_withExistingObject_shouldReturnTrue() {
    // Arrange
    String storageKey = "newspaper/2026/08/15/bhopal-city/issue.pdf";

    HeadObjectResponse mockResponse = HeadObjectResponse.builder().build();
    when(s3Client.headObject((HeadObjectRequest) any()))
            .thenReturn(mockResponse);

    // Act
    boolean result = storageService.exists(storageKey);

    // Assert
    assertTrue(result);
    verify(s3Client, times(1)).headObject((HeadObjectRequest) any());
  }

  @Test
  void exists_withNonExistentObject_shouldReturnFalse() {
    // Arrange
    String storageKey = "newspaper/2026/08/15/non-existent/issue.pdf";

    when(s3Client.headObject((HeadObjectRequest) any()))
            .thenThrow(NoSuchKeyException.builder().message("Key not found").build());

    // Act
    boolean result = storageService.exists(storageKey);

    // Assert
    assertFalse(result);
    verify(s3Client, times(1)).headObject((HeadObjectRequest) any());
  }

  @Test
  void exists_withS3Exception_shouldReturnFalse() {
    // Arrange
    String storageKey = "newspaper/2026/08/15/bhopal-city/issue.pdf";

    S3Exception s3Exception = (S3Exception) S3Exception.builder()
            .message("Access denied")
            .statusCode(403)
            .build();

    when(s3Client.headObject((HeadObjectRequest) any()))
            .thenThrow(s3Exception);

    // Act
    boolean result = storageService.exists(storageKey);

    // Assert
    assertFalse(result);
    verify(s3Client, times(1)).headObject((HeadObjectRequest) any());
  }

  @Test
  void upload_shouldUseBucketFromProperties() {
    // Arrange
    String storageKey = "newspaper/2026/08/15/bhopal-city/issue.pdf";
    byte[] content = "test pdf content".getBytes();
    InputStream inputStream = new ByteArrayInputStream(content);

    StorageUploadRequest request = StorageUploadRequest.builder()
            .inputStream(inputStream)
            .contentType("application/pdf")
            .fileSize((long) content.length)
            .storageKey(storageKey)
            .build();

    PutObjectResponse mockResponse = PutObjectResponse.builder()
            .eTag("mock-etag")
            .build();

    when(s3Client.putObject((PutObjectRequest) any(), any(RequestBody.class)))
            .thenReturn(mockResponse);

    // Act
    storageService.upload(request);

    // Assert
    verify(s3Client).putObject(argThat((PutObjectRequest putRequest) ->
            putRequest.bucket().equals("test-bucket") &&
                    putRequest.key().equals(storageKey) &&
                    putRequest.contentType().equals("application/pdf")
    ), any(RequestBody.class));
  }

  @Test
  void delete_shouldUseBucketFromProperties() {
    // Arrange
    String storageKey = "newspaper/2026/08/15/bhopal-city/issue.pdf";

    DeleteObjectResponse mockResponse = DeleteObjectResponse.builder().build();
    when(s3Client.deleteObject((DeleteObjectRequest) any()))
            .thenReturn(mockResponse);

    // Act
    storageService.delete(storageKey);

    // Assert
    verify(s3Client).deleteObject(argThat((DeleteObjectRequest deleteRequest) ->
            deleteRequest.bucket().equals("test-bucket") &&
                    deleteRequest.key().equals(storageKey)
    ));
  }

  @Test
  void exists_shouldUseBucketFromProperties() {
    // Arrange
    String storageKey = "newspaper/2026/08/15/bhopal-city/issue.pdf";

    HeadObjectResponse mockResponse = HeadObjectResponse.builder().build();
    when(s3Client.headObject((HeadObjectRequest) any()))
            .thenReturn(mockResponse);

    // Act
    storageService.exists(storageKey);

    // Assert
    verify(s3Client).headObject(argThat((HeadObjectRequest headRequest) ->
            headRequest.bucket().equals("test-bucket") &&
                    headRequest.key().equals(storageKey)
    ));
  }
}
