package com.newspaper.library.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;
import org.springframework.lang.NonNull;

import java.time.Instant;
import java.util.List;

/**
 * Paginated API response wrapper following REST best practices.
 * Provides consistent pagination structure for collection endpoints.
 *
 * @param <T> the type of data being returned
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Paginated API response")
public record PageResponse<T>(
        @NonNull @Schema(description = "List of items in current page", requiredMode = Schema.RequiredMode.REQUIRED) List<T> data,
        @NonNull @Schema(description = "Pagination metadata", requiredMode = Schema.RequiredMode.REQUIRED) PaginationInfo pagination,
        @NonNull @Schema(description = "Timestamp when response was generated", requiredMode = Schema.RequiredMode.REQUIRED, example = "2026-08-15T18:00:00Z") Instant timestamp) {

  /**
   * Create paginated response from Spring Data Page.
   */
  public static <T> PageResponse<T> of(@NonNull List<T> data, @NonNull Page<?> page) {
    return new PageResponse<>(data, PaginationInfo.from(page), Instant.now());
  }

  /**
   * Pagination information with 1-based page numbers for user-facing API.
   */
  @Schema(description = "Pagination information")
  public record PaginationInfo(
          @Schema(description = "Current page number (1-indexed)", requiredMode = Schema.RequiredMode.REQUIRED, example = "1") int page,
          @Schema(description = "Number of items per page", requiredMode = Schema.RequiredMode.REQUIRED, example = "20") int size,
          @Schema(description = "Total number of items across all pages", requiredMode = Schema.RequiredMode.REQUIRED, example = "100") long totalElements,
          @Schema(description = "Total number of pages", requiredMode = Schema.RequiredMode.REQUIRED, example = "5") int totalPages,
          @Schema(description = "Whether this is the first page", requiredMode = Schema.RequiredMode.REQUIRED, example = "true") boolean first,
          @Schema(description = "Whether this is the last page", requiredMode = Schema.RequiredMode.REQUIRED, example = "false") boolean last,
          @Schema(description = "Whether there is a next page available", requiredMode = Schema.RequiredMode.REQUIRED, example = "true") boolean hasNext,
          @Schema(description = "Whether there is a previous page available", requiredMode = Schema.RequiredMode.REQUIRED, example = "false") boolean hasPrevious) {

    /**
     * Create PaginationInfo from Spring Data Page.
     * Converts 0-based page number to 1-based for user-facing API.
     */
    public static PaginationInfo from(Page<?> page) {
      return new PaginationInfo(
              page.getNumber() + 1,  // Convert 0-based to 1-based
              page.getSize(),
              page.getTotalElements(),
              page.getTotalPages(),
              page.isFirst(),
              page.isLast(),
              page.hasNext(),
              page.hasPrevious()
      );
    }
  }
}
