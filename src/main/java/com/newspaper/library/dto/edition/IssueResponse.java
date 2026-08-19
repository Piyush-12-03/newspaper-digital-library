package com.newspaper.library.dto.edition;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;

/**
 * DTO for issue response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response containing issue details")
public class IssueResponse {

  @Schema(description = "Unique identifier of the issue", example = "1")
  private Long id;

  @Schema(description = "ID of the edition this issue belongs to", example = "1")
  private Long editionId;

  @Schema(description = "Name of the edition", example = "Bhopal City")
  private String editionName;

  @Schema(description = "Publication date of the issue", example = "2026-08-15")
  private LocalDate publicationDate;

  @Schema(description = "Original filename of the PDF", example = "bhopal-city-2026-08-15.pdf")
  private String fileName;

  @Schema(description = "MIME type of the file", example = "application/pdf")
  private String contentType;

  @Schema(description = "File size in bytes", example = "2048576")
  private Long fileSize;

  @Schema(description = "Number of pages in the PDF", example = "12")
  private Integer pageCount;

  @Schema(description = "Timestamp when the issue was created")
  private Instant createdAt;

  @Schema(description = "Timestamp when the issue was last updated")
  private Instant updatedAt;
}
