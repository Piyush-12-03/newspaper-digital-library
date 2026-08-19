package com.newspaper.library.controller;

import com.newspaper.library.config.NewspaperApiProperties;
import com.newspaper.library.dto.common.GenericResponse;
import com.newspaper.library.dto.common.PageResponse;
import com.newspaper.library.dto.issue.IssueDetailResponse;
import com.newspaper.library.dto.issue.IssueDownloadUrlResponse;
import com.newspaper.library.dto.issue.IssueSummaryResponse;
import com.newspaper.library.dto.issue.IssueUploadResponse;
import com.newspaper.library.exception.InvalidPageSizeException;
import com.newspaper.library.service.IssueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;

/**
 * Issue management controller.
 */
@Slf4j
@RestController
@RequestMapping("/issues")
@RequiredArgsConstructor
@Tag(name = "Issues", description = "Newspaper issue management")
public class IssueController {

  private final IssueService issueService;
  private final NewspaperApiProperties apiProperties;

  @Operation(
          summary = "Upload PDF to S3 (Admin)",
          description = """
                  Upload newspaper PDF for a specific edition and date.
                  
                  **How to use:**
                  1. First, get edition IDs: GET /editions
                  2. Select the edition (e.g., Bhopal City = ID 1)
                  3. Upload PDF: POST /issues?editionId=1&publicationDate=2026-08-12&file=...
                  
                  **Duplicate Prevention:**
                  - Cannot upload same edition + date twice
                  - Returns 409 CONFLICT if already exists
                  
                  **Storage:**
                  - PDF stored in S3
                  - Metadata stored in PostgreSQL
                  """,
          security = @SecurityRequirement(name = "Bearer Authentication"),
          responses = {
                  @ApiResponse(responseCode = "201", description = "PDF uploaded to S3"),
                  @ApiResponse(responseCode = "400", description = "Invalid file or parameters"),
                  @ApiResponse(responseCode = "401", description = "Unauthorized"),
                  @ApiResponse(responseCode = "404", description = "Edition not found"),
                  @ApiResponse(responseCode = "409", description = "Duplicate issue - already exists for this edition and date")
          }
  )
  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public GenericResponse<IssueUploadResponse> uploadIssue(
          @RequestParam Long editionId,
          @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate publicationDate,
          @RequestParam MultipartFile file) {

    log.info("POST /api/v1/issues - Edition ID: {}, Date: {}, Size: {} bytes",
            editionId, publicationDate, file.getSize());
    IssueUploadResponse data = issueService.uploadIssue(editionId, publicationDate, file);
    return GenericResponse.success(HttpStatus.CREATED.value(), data, "PDF uploaded successfully to S3");
  }

  @Operation(
          summary = "List issues",
          description = "List with optional filters (date, date range, edition) and pagination. Page starts from 1."
  )
  @GetMapping
  public GenericResponse<PageResponse<IssueSummaryResponse>> getIssues(
          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
          @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
          @RequestParam(required = false) Long editionId,
          @RequestParam(required = false, defaultValue = "1") int page,
          @RequestParam(required = false) Integer size,
          @RequestParam(required = false, defaultValue = "publicationDate,desc") String sort) {

    log.info("GET /api/v1/issues - date:{}, from:{}, to:{}, edition:{}, page:{}", date, from, to, editionId, page);

    // Validate page number (must be >= 1)
    if (page < 1) {
      throw new InvalidPageSizeException("Page number must be 1 or greater");
    }

    int pageSize = (size != null) ? size : apiProperties.getDefaultPageSize();

    if (pageSize < 1 || pageSize > apiProperties.getMaxPageSize()) {
      throw new InvalidPageSizeException(
              String.format("Page size must be between 1 and %d", apiProperties.getMaxPageSize()));
    }

    // Convert 1-based page to 0-based for Spring Data
    int zeroBasedPage = page - 1;
    Pageable pageable = buildPageable(zeroBasedPage, pageSize, sort);
    Page<IssueSummaryResponse> issues = issueService.getIssues(date, from, to, editionId, pageable);

    PageResponse<IssueSummaryResponse> data = PageResponse.of(issues.getContent(), issues);
    return GenericResponse.success(data, "Issues retrieved successfully");
  }

  @Operation(
          summary = "Get download URL for PDF (Public)",
          description = "Returns a presigned S3 URL that can be used to download the PDF directly. " +
                  "The URL expires in 10 minutes and the file downloads with its original filename.",
          responses = {
                  @ApiResponse(responseCode = "200", description = "Download URL generated"),
                  @ApiResponse(responseCode = "404", description = "Issue not found")
          }
  )
  @GetMapping("/{issueId}/download-url")
  public GenericResponse<IssueDownloadUrlResponse> getDownloadUrl(@PathVariable Long issueId) {
    log.info("GET /api/v1/issues/{}/download-url - Generating presigned URL", issueId);
    IssueDownloadUrlResponse data = issueService.generateDownloadUrl(issueId);
    return GenericResponse.success(data, "Download URL generated successfully");
  }

  @Operation(
          summary = "Get issue metadata by ID (Public)",
          description = "Returns only metadata - PDF details without downloading the actual file",
          responses = {
                  @ApiResponse(responseCode = "200", description = "Issue metadata retrieved"),
                  @ApiResponse(responseCode = "404", description = "Issue not found")
          }
  )
  @GetMapping("/{issueId}")
  public GenericResponse<IssueDetailResponse> getIssueById(@PathVariable Long issueId) {
    log.info("GET /api/v1/issues/{} - Fetching metadata only", issueId);
    IssueDetailResponse data = issueService.getIssueById(issueId);
    return GenericResponse.success(data, "Issue metadata retrieved successfully");
  }

  private Pageable buildPageable(int page, int size, String sortParam) {
    String[] sortParts = sortParam.split(",");
    String sortField = sortParts[0];
    Sort.Direction direction = (sortParts.length > 1 && "asc".equalsIgnoreCase(sortParts[1]))
            ? Sort.Direction.ASC
            : Sort.Direction.DESC;

    List<String> allowedSortFields = List.of("publicationDate", "createdAt", "id");
    if (!allowedSortFields.contains(sortField)) {
      log.warn("Invalid sort field: {}. Using default", sortField);
      sortField = "publicationDate";
    }

    return PageRequest.of(page, size, Sort.by(direction, sortField));
  }
}
