package com.newspaper.library.service;

import com.newspaper.library.dto.issue.IssueDetailResponse;
import com.newspaper.library.dto.issue.IssueDownloadUrlResponse;
import com.newspaper.library.dto.issue.IssueSummaryResponse;
import com.newspaper.library.dto.issue.IssueUploadResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDate;

/**
 * Service interface for issue operations.
 */
public interface IssueService {

  /**
   * Upload a PDF issue for a specific edition and date.
   * Uploads PDF to S3 and stores metadata in PostgreSQL.
   * Admin only operation.
   */
  IssueUploadResponse uploadIssue(Long editionId, LocalDate publicationDate, MultipartFile file);

  /**
   * Get a single issue by ID with metadata only.
   */
  IssueDetailResponse getIssueById(Long issueId);

  /**
   * Generate a presigned download URL for an issue's PDF.
   * The URL expires after 10 minutes.
   *
   * @param issueId issue ID
   * @return Download URL response with metadata
   */
  IssueDownloadUrlResponse generateDownloadUrl(Long issueId);

  /**
   * Get issues with optional filtering.
   *
   * @param date      specific date (optional)
   * @param from      start date for range (optional)
   * @param to        end date for range (optional)
   * @param editionId filter by edition (optional)
   * @param pageable  pagination and sorting
   * @return paginated list of issues
   */
  Page<IssueSummaryResponse> getIssues(
          LocalDate date,
          LocalDate from,
          LocalDate to,
          Long editionId,
          Pageable pageable
  );
}
