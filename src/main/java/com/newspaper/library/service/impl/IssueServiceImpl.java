package com.newspaper.library.service.impl;

import com.newspaper.library.config.NewspaperApiProperties;
import com.newspaper.library.dto.issue.IssueDetailResponse;
import com.newspaper.library.dto.issue.IssueDownloadUrlResponse;
import com.newspaper.library.dto.issue.IssueSummaryResponse;
import com.newspaper.library.dto.issue.IssueUploadResponse;
import com.newspaper.library.dto.storage.StorageUploadRequest;
import com.newspaper.library.dto.storage.StorageUploadResult;
import com.newspaper.library.entity.Edition;
import com.newspaper.library.entity.Issue;
import com.newspaper.library.enums.ApiErrorCode;
import com.newspaper.library.exception.DuplicateResourceException;
import com.newspaper.library.exception.InvalidDateRangeException;
import com.newspaper.library.exception.InvalidFileException;
import com.newspaper.library.exception.ResourceNotFoundException;
import com.newspaper.library.mapper.IssueMapper;
import com.newspaper.library.repository.EditionRepository;
import com.newspaper.library.repository.IssueRepository;
import com.newspaper.library.service.IssueService;
import com.newspaper.library.service.PdfProcessingService;
import com.newspaper.library.service.PdfStorageService;
import com.newspaper.library.util.S3StorageKeyBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Implementation of issue service.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IssueServiceImpl implements IssueService {

  private final IssueRepository issueRepository;
  private final EditionRepository editionRepository;
  private final PdfStorageService pdfStorageService;
  private final PdfProcessingService pdfProcessingService;
  private final IssueMapper issueMapper;
  private final NewspaperApiProperties apiProperties;

  @Override
  @Transactional
  public IssueUploadResponse uploadIssue(Long editionId, LocalDate publicationDate, MultipartFile file) {
    log.info("Uploading PDF for edition ID '{}' on {}", editionId, publicationDate);

    // Validate file
    if (file.isEmpty()) {
      throw new InvalidFileException("File is empty");
    }

    if (!Objects.equals(file.getContentType(), "application/pdf")) {
      throw new InvalidFileException("Only PDF files are allowed. Received: " + file.getContentType());
    }

    // Find edition by ID (fast primary key lookup)
    Edition edition = editionRepository.findById(editionId)
            .orElseThrow(() -> new ResourceNotFoundException("Edition not found with id: " + editionId));

    // Check if issue already exists for this edition and date
    if (issueRepository.existsByEditionIdAndPublicationDate(edition.getId(), publicationDate)) {
      throw new DuplicateResourceException(
              String.format("Issue already exists for edition '%s' on %s", edition.getName(), publicationDate));
    }

    // Extract PDF page count before upload
    Integer pageCount = null;
    try {
      log.debug("Extracting PDF page count");
      pageCount = pdfProcessingService.extractPageCount(file.getInputStream());
      log.info("PDF contains {} pages", pageCount);
    } catch (Exception e) {
      log.warn("Failed to extract page count from PDF, will continue without it", e);
      // Continue upload even if page count extraction fails
    }

    // Build deterministic S3 storage key with original filename
    String storageKey = S3StorageKeyBuilder.buildKey(edition.getSlug(), publicationDate, file.getOriginalFilename());
    log.debug("Generated storage key: {}", storageKey);

    // Upload PDF to S3
    StorageUploadResult uploadResult;
    try {
      StorageUploadRequest uploadRequest = StorageUploadRequest.builder()
              .inputStream(file.getInputStream())
              .contentType(file.getContentType())
              .fileSize(file.getSize())
              .storageKey(storageKey)
              .build();

      uploadResult = pdfStorageService.upload(uploadRequest);
      log.info("PDF uploaded to S3 successfully. Key: {}, ETag: {}", uploadResult.getStorageKey(), uploadResult.getEtag());

    } catch (Exception e) {
      log.error("Failed to upload PDF to S3 for edition ID '{}' on {}", editionId, publicationDate, e);
      throw new InvalidFileException("Failed to upload PDF: " + e.getMessage());
    }

    // Save issue metadata to PostgreSQL
    Issue issue = new Issue();
    issue.setEdition(edition);
    issue.setPublicationDate(publicationDate);
    issue.setFileName(file.getOriginalFilename());
    issue.setStorageKey(uploadResult.getStorageKey());
    issue.setContentType(uploadResult.getContentType());
    issue.setFileSize(uploadResult.getFileSize());
    issue.setPageCount(pageCount);

    Issue savedIssue;
    try {
      savedIssue = issueRepository.save(issue);
      log.info("Issue metadata saved to database. Issue ID: {}, Pages: {}", savedIssue.getId(), pageCount);
    } catch (Exception e) {
      // Rollback: Delete uploaded file from S3 if database save fails
      log.error("Database save failed, cleaning up S3 object: {}", storageKey, e);
      try {
        pdfStorageService.delete(storageKey);
        log.info("S3 cleanup successful for key: {}", storageKey);
      } catch (Exception cleanupException) {
        log.error("Failed to cleanup S3 object after database failure: {}", storageKey, cleanupException);
      }
      throw e;
    }

    return IssueUploadResponse.builder()
            .issueId(savedIssue.getId())
            .editionName(edition.getName())
            .publicationDate(publicationDate)
            .fileName(file.getOriginalFilename())
            .fileSize(file.getSize())
            .storagePath(uploadResult.getStorageKey())
            .uploadedAt(Instant.now())
            .message(String.format("PDF uploaded successfully to S3 (%d pages)", pageCount != null ? pageCount : 0))
            .build();
  }

  @Override
  @Transactional(readOnly = true)
  public IssueDownloadUrlResponse generateDownloadUrl(Long issueId) {
    log.info("Generating presigned download URL for issue ID: {}", issueId);

    Issue issue = issueRepository.findById(issueId)
            .orElseThrow(() -> new ResourceNotFoundException("Issue not found with issueId: " + issueId));

    log.info("Generating presigned URL for Issue: {}, Edition: {}, Date: {}, FileName: {}",
            issueId, issue.getEdition().getName(), issue.getPublicationDate(), issue.getFileName());

    // Generate presigned URL with the original filename
    String presignedUrl = pdfStorageService.generatePresignedDownloadUrl(
            issue.getStorageKey(),
            issue.getFileName()
    );

    log.info("Presigned URL generated for issue ID: {}", issueId);

    return IssueDownloadUrlResponse.builder()
            .issueId(issue.getId())
            .fileName(issue.getFileName())
            .fileSize(issue.getFileSize())
            .pageCount(issue.getPageCount())
            .downloadUrl(presignedUrl)
            .expiresInMinutes(10)
            .build();
  }

  @Override
  @Transactional(readOnly = true)
  public IssueDetailResponse getIssueById(Long issueId) {
    log.debug("Fetching issue with issueId: {}", issueId);
    Issue issue = issueRepository.findById(issueId)
            .orElseThrow(() -> new ResourceNotFoundException("Issue not found with issueId: " + issueId));

    log.info("Found issue for edition '{}' on {}", issue.getEdition().getName(), issue.getPublicationDate());
    return issueMapper.toDetail(issue);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<IssueSummaryResponse> getIssues(
          LocalDate date,
          LocalDate from,
          LocalDate to,
          Long editionId,
          Pageable pageable) {

    // Validate conflicting filters
    if (date != null && (from != null || to != null)) {
      throw new InvalidDateRangeException(
              "Cannot specify both 'date' and date range ('from'/'to'). Use one or the other.",
              ApiErrorCode.INVALID_REQUEST
      );
    }

    // If specific date is provided
    if (date != null) {
      log.debug("Fetching issues for date: {}, editionId: {}", date, editionId);
      return fetchIssuesByDateAndEdition(date, editionId, pageable);
    }

    // If date range is provided
    if (from != null || to != null) {
      validateDateRange(from, to);
      log.debug("Fetching issues from {} to {}, editionId: {}", from, to, editionId);
      return fetchIssuesByDateRangeAndEdition(from, to, editionId, pageable);
    }

    // No date filter
    if (editionId != null) {
      log.debug("Fetching all issues for edition: {}", editionId);
      return issueRepository.findByEditionId(editionId, pageable)
              .map(issueMapper::toSummary);
    }

    log.debug("Fetching all issues");
    return issueRepository.findAll(pageable)
            .map(issueMapper::toSummary);
  }

  private Page<IssueSummaryResponse> fetchIssuesByDateAndEdition(LocalDate date, Long editionId, Pageable pageable) {
    if (editionId != null) {
      return issueRepository.findByPublicationDateAndEditionId(date, editionId, pageable)
              .map(issueMapper::toSummary);
    }
    return issueRepository.findByPublicationDate(date, pageable)
            .map(issueMapper::toSummary);
  }

  private Page<IssueSummaryResponse> fetchIssuesByDateRangeAndEdition(
          LocalDate from, LocalDate to, Long editionId, Pageable pageable) {

    if (editionId != null) {
      return issueRepository.findByPublicationDateBetweenAndEditionId(from, to, editionId, pageable)
              .map(issueMapper::toSummary);
    }
    return issueRepository.findByPublicationDateBetween(from, to, pageable)
            .map(issueMapper::toSummary);
  }

  private void validateDateRange(LocalDate from, LocalDate to) {
    if (from == null || to == null) {
      throw new InvalidDateRangeException(
              "Both 'from' and 'to' dates must be specified for date range queries.",
              ApiErrorCode.INVALID_REQUEST
      );
    }

    if (from.isAfter(to)) {
      throw new InvalidDateRangeException(
              "The 'from' date cannot be after the 'to' date.",
              ApiErrorCode.INVALID_DATE_RANGE
      );
    }

    long daysBetween = ChronoUnit.DAYS.between(from, to);
    if (daysBetween > apiProperties.getMaxDateRangeDays()) {
      throw new InvalidDateRangeException(
              String.format("Date range cannot exceed %d days. Requested: %d days.",
                      apiProperties.getMaxDateRangeDays(), daysBetween),
              ApiErrorCode.DATE_RANGE_TOO_LARGE
      );
    }
  }
}
