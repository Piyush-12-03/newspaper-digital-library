package com.newspaper.library.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Utility class for building deterministic S3 storage keys.
 * <p>
 * Structure: newspaper/{yyyy}/{MM}/{dd}/{edition-slug}/{filename}
 * <p>
 * Example: newspaper/2026/08/15/bhopal-city/bhopal-city-2026-08-15.pdf
 */
public final class S3StorageKeyBuilder {

  private static final String BASE_PREFIX = "newspaper";
  private static final DateTimeFormatter YEAR_FORMATTER = DateTimeFormatter.ofPattern("yyyy");
  private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("MM");
  private static final DateTimeFormatter DAY_FORMATTER = DateTimeFormatter.ofPattern("dd");

  private S3StorageKeyBuilder() {
    // Utility class
  }

  /**
   * Build deterministic S3 storage key from edition slug, publication date, and filename.
   *
   * @param editionSlug     Edition slug (e.g., "bhopal-city")
   * @param publicationDate Publication date
   * @param fileName        Original filename (e.g., "bhopal-city-2026-08-15.pdf")
   * @return S3 storage key (e.g., "newspaper/2026/08/15/bhopal-city/bhopal-city-2026-08-15.pdf")
   * @throws IllegalArgumentException if parameters are null or invalid
   */
  public static String buildKey(String editionSlug, LocalDate publicationDate, String fileName) {
    if (editionSlug == null || editionSlug.isBlank()) {
      throw new IllegalArgumentException("Edition slug must not be null or blank");
    }
    if (publicationDate == null) {
      throw new IllegalArgumentException("Publication date must not be null");
    }
    if (fileName == null || fileName.isBlank()) {
      throw new IllegalArgumentException("Filename must not be null or blank");
    }

    String year = publicationDate.format(YEAR_FORMATTER);
    String month = publicationDate.format(MONTH_FORMATTER);
    String day = publicationDate.format(DAY_FORMATTER);

    return String.format("%s/%s/%s/%s/%s/%s",
            BASE_PREFIX,
            year,
            month,
            day,
            editionSlug,
            fileName);
  }
}
