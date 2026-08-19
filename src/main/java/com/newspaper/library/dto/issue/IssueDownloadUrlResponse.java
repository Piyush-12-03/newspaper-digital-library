package com.newspaper.library.dto.issue;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response containing presigned download URL for an issue's PDF.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Presigned download URL for PDF")
public class IssueDownloadUrlResponse {

  @Schema(description = "Issue ID", example = "123")
  private Long issueId;

  @Schema(description = "Original filename", example = "bhopal-city-2026-08-18.pdf")
  private String fileName;

  @Schema(description = "File size in bytes", example = "2048576")
  private Long fileSize;

  @Schema(description = "Number of pages", example = "12")
  private Integer pageCount;

  @Schema(description = "Presigned download URL (expires in 10 minutes)",
          example = "https://s3.amazonaws.com/bucket/path?signature=...")
  private String downloadUrl;

  @Schema(description = "URL expiration time in minutes", example = "10")
  private Integer expiresInMinutes;
}
