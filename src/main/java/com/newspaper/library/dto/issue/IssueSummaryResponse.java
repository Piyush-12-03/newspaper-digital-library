package com.newspaper.library.dto.issue;

import com.newspaper.library.dto.edition.EditionSummaryResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Lightweight issue summary for list responses.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Issue summary information")
public class IssueSummaryResponse {

  @Schema(description = "Unique identifier of the issue", example = "1")
  private Long id;

  @Schema(description = "Edition information")
  private EditionSummaryResponse edition;

  @Schema(description = "Publication date of the issue", example = "2026-08-15")
  private LocalDate publicationDate;

  @Schema(description = "Original filename of the PDF", example = "bhopal-city-2026-08-15.pdf")
  private String fileName;

  @Schema(description = "File size in bytes", example = "2048576")
  private Long fileSize;

  @Schema(description = "Number of pages in the PDF", example = "12")
  private Integer pageCount;
}
