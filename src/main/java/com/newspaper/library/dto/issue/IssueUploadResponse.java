package com.newspaper.library.dto.issue;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Response DTO for successful PDF upload.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response after successful PDF upload")
public class IssueUploadResponse {

  @Schema(description = "ID of the created issue", example = "123")
  private Long issueId;

  @Schema(description = "Edition name", example = "Bhopal City")
  private String editionName;

  @Schema(description = "Publication date", example = "2026-08-12")
  private LocalDate publicationDate;

  @Schema(description = "Uploaded file name", example = "bhopal-city-2026-08-12.pdf")
  private String fileName;

  @Schema(description = "File size in bytes", example = "2048576")
  private Long fileSize;

  @Schema(description = "Storage path or S3 URL", example = "s3://bucket/bhopal-city-2026-08-12.pdf")
  private String storagePath;

  @Schema(description = "Upload timestamp", example = "2026-08-12T10:30:00Z")
  private Instant uploadedAt;

  @Schema(description = "Success message", example = "PDF uploaded successfully")
  private String message;
}
