package com.newspaper.library.config.properties;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Typed configuration properties for S3 storage.
 * Validated at startup to prevent misconfiguration.
 */
@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "newspaper.storage.s3")
public class S3Properties {

  /**
   * S3 bucket name
   */
  @NotBlank(message = "S3 bucket name is required")
  private String bucket;

  /**
   * AWS region (e.g., us-east-1)
   */
  @NotBlank(message = "AWS region is required")
  private String region;

  /**
   * Maximum file size in MB
   */
  @Min(value = 1, message = "Max file size must be at least 1 MB")
  private int maxFileSizeMb = 50;

  /**
   * Presigned URL expiration in minutes
   */
  @Min(value = 1, message = "URL expiration must be at least 1 minute")
  private int downloadUrlExpirationMinutes = 10;

  /**
   * Connection timeout in milliseconds
   */
  @Min(value = 1000, message = "Connection timeout must be at least 1000ms")
  private int connectionTimeoutMs = 5000;

  /**
   * API call timeout in milliseconds
   */
  @Min(value = 5000, message = "API call timeout must be at least 5000ms")
  private int apiCallTimeoutMs = 30000;

  /**
   * Get max file size in bytes
   */
  public long getMaxFileSizeBytes() {
    return (long) maxFileSizeMb * 1024 * 1024;
  }
}
