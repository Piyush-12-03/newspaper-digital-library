package com.newspaper.library.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for S3StorageKeyBuilder.
 * Verifies deterministic key generation with proper formatting.
 */
class S3StorageKeyBuilderTest {

  @Test
  void buildKey_withValidInputs_shouldReturnCorrectFormat() {
    String editionSlug = "bhopal-city";
    LocalDate publicationDate = LocalDate.of(2026, 8, 15);
    String fileName = "bhopal-city-2026-08-15.pdf";

    String result = S3StorageKeyBuilder.buildKey(editionSlug, publicationDate, fileName);

    assertEquals("newspaper/2026/08/15/bhopal-city/bhopal-city-2026-08-15.pdf", result);
  }

  @Test
  void buildKey_withSingleDigitMonth_shouldZeroPad() {
    String editionSlug = "indore-city";
    LocalDate publicationDate = LocalDate.of(2026, 1, 5);
    String fileName = "indore-city-2026-01-05.pdf";

    String result = S3StorageKeyBuilder.buildKey(editionSlug, publicationDate, fileName);

    assertEquals("newspaper/2026/01/05/indore-city/indore-city-2026-01-05.pdf", result);
  }

  @Test
  void buildKey_withSingleDigitDay_shouldZeroPad() {
    String editionSlug = "jabalpur";
    LocalDate publicationDate = LocalDate.of(2026, 12, 3);
    String fileName = "jabalpur-2026-12-03.pdf";

    String result = S3StorageKeyBuilder.buildKey(editionSlug, publicationDate, fileName);

    assertEquals("newspaper/2026/12/03/jabalpur/jabalpur-2026-12-03.pdf", result);
  }

  @Test
  void buildKey_withDoubleDigitMonthAndDay_shouldNotAddExtraPadding() {
    String editionSlug = "gwalior";
    LocalDate publicationDate = LocalDate.of(2026, 11, 25);
    String fileName = "gwalior-2026-11-25.pdf";

    String result = S3StorageKeyBuilder.buildKey(editionSlug, publicationDate, fileName);

    assertEquals("newspaper/2026/11/25/gwalior/gwalior-2026-11-25.pdf", result);
  }

  @Test
  void buildKey_withDifferentEditions_shouldProduceDifferentKeys() {
    LocalDate publicationDate = LocalDate.of(2026, 8, 15);
    String fileName1 = "bhopal-city-2026-08-15.pdf";
    String fileName2 = "indore-city-2026-08-15.pdf";

    String key1 = S3StorageKeyBuilder.buildKey("bhopal-city", publicationDate, fileName1);
    String key2 = S3StorageKeyBuilder.buildKey("indore-city", publicationDate, fileName2);

    assertNotEquals(key1, key2);
    assertEquals("newspaper/2026/08/15/bhopal-city/bhopal-city-2026-08-15.pdf", key1);
    assertEquals("newspaper/2026/08/15/indore-city/indore-city-2026-08-15.pdf", key2);
  }

  @Test
  void buildKey_withDifferentDates_shouldProduceDifferentKeys() {
    String editionSlug = "bhopal-city";
    String fileName1 = "bhopal-city-2026-08-15.pdf";
    String fileName2 = "bhopal-city-2026-08-16.pdf";

    String key1 = S3StorageKeyBuilder.buildKey(editionSlug, LocalDate.of(2026, 8, 15), fileName1);
    String key2 = S3StorageKeyBuilder.buildKey(editionSlug, LocalDate.of(2026, 8, 16), fileName2);

    assertNotEquals(key1, key2);
    assertEquals("newspaper/2026/08/15/bhopal-city/bhopal-city-2026-08-15.pdf", key1);
    assertEquals("newspaper/2026/08/16/bhopal-city/bhopal-city-2026-08-16.pdf", key2);
  }

  @Test
  void buildKey_withDifferentFilenames_shouldProduceDifferentKeys() {
    String editionSlug = "bhopal-city";
    LocalDate publicationDate = LocalDate.of(2026, 8, 15);

    String key1 = S3StorageKeyBuilder.buildKey(editionSlug, publicationDate, "morning-edition.pdf");
    String key2 = S3StorageKeyBuilder.buildKey(editionSlug, publicationDate, "evening-edition.pdf");

    assertNotEquals(key1, key2);
    assertEquals("newspaper/2026/08/15/bhopal-city/morning-edition.pdf", key1);
    assertEquals("newspaper/2026/08/15/bhopal-city/evening-edition.pdf", key2);
  }

  @Test
  void buildKey_withNullEditionSlug_shouldThrowException() {
    LocalDate publicationDate = LocalDate.of(2026, 8, 15);
    String fileName = "test.pdf";

    IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> S3StorageKeyBuilder.buildKey(null, publicationDate, fileName)
    );

    assertEquals("Edition slug must not be null or blank", exception.getMessage());
  }

  @Test
  void buildKey_withBlankEditionSlug_shouldThrowException() {
    LocalDate publicationDate = LocalDate.of(2026, 8, 15);
    String fileName = "test.pdf";

    IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> S3StorageKeyBuilder.buildKey("   ", publicationDate, fileName)
    );

    assertEquals("Edition slug must not be null or blank", exception.getMessage());
  }

  @Test
  void buildKey_withEmptyEditionSlug_shouldThrowException() {
    LocalDate publicationDate = LocalDate.of(2026, 8, 15);
    String fileName = "test.pdf";

    IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> S3StorageKeyBuilder.buildKey("", publicationDate, fileName)
    );

    assertEquals("Edition slug must not be null or blank", exception.getMessage());
  }

  @Test
  void buildKey_withNullPublicationDate_shouldThrowException() {
    String editionSlug = "bhopal-city";
    String fileName = "test.pdf";

    IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> S3StorageKeyBuilder.buildKey(editionSlug, null, fileName)
    );

    assertEquals("Publication date must not be null", exception.getMessage());
  }

  @Test
  void buildKey_withNullFilename_shouldThrowException() {
    String editionSlug = "bhopal-city";
    LocalDate publicationDate = LocalDate.of(2026, 8, 15);

    IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> S3StorageKeyBuilder.buildKey(editionSlug, publicationDate, null)
    );

    assertEquals("Filename must not be null or blank", exception.getMessage());
  }

  @Test
  void buildKey_withBlankFilename_shouldThrowException() {
    String editionSlug = "bhopal-city";
    LocalDate publicationDate = LocalDate.of(2026, 8, 15);

    IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> S3StorageKeyBuilder.buildKey(editionSlug, publicationDate, "   ")
    );

    assertEquals("Filename must not be null or blank", exception.getMessage());
  }

  @Test
  void buildKey_withEmptyFilename_shouldThrowException() {
    String editionSlug = "bhopal-city";
    LocalDate publicationDate = LocalDate.of(2026, 8, 15);

    IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> S3StorageKeyBuilder.buildKey(editionSlug, publicationDate, "")
    );

    assertEquals("Filename must not be null or blank", exception.getMessage());
  }

  @Test
  void buildKey_withLeapYearDate_shouldHandleCorrectly() {
    String editionSlug = "bhopal-city";
    LocalDate leapYearDate = LocalDate.of(2024, 2, 29);
    String fileName = "bhopal-city-2024-02-29.pdf";

    String result = S3StorageKeyBuilder.buildKey(editionSlug, leapYearDate, fileName);

    assertEquals("newspaper/2024/02/29/bhopal-city/bhopal-city-2024-02-29.pdf", result);
  }

  @Test
  void buildKey_withDecember31_shouldHandleCorrectly() {
    String editionSlug = "indore-city";
    LocalDate yearEnd = LocalDate.of(2025, 12, 31);
    String fileName = "indore-city-2025-12-31.pdf";

    String result = S3StorageKeyBuilder.buildKey(editionSlug, yearEnd, fileName);

    assertEquals("newspaper/2025/12/31/indore-city/indore-city-2025-12-31.pdf", result);
  }

  @Test
  void buildKey_withJanuary1_shouldHandleCorrectly() {
    String editionSlug = "jabalpur";
    LocalDate yearStart = LocalDate.of(2027, 1, 1);
    String fileName = "jabalpur-2027-01-01.pdf";

    String result = S3StorageKeyBuilder.buildKey(editionSlug, yearStart, fileName);

    assertEquals("newspaper/2027/01/01/jabalpur/jabalpur-2027-01-01.pdf", result);
  }

  @Test
  void buildKey_withSlugContainingHyphens_shouldPreserveFormat() {
    String editionSlug = "mumbai-central-edition";
    LocalDate publicationDate = LocalDate.of(2026, 8, 15);
    String fileName = "mumbai-central-edition-2026-08-15.pdf";

    String result = S3StorageKeyBuilder.buildKey(editionSlug, publicationDate, fileName);

    assertEquals("newspaper/2026/08/15/mumbai-central-edition/mumbai-central-edition-2026-08-15.pdf", result);
  }

  @Test
  void buildKey_shouldBeConsistentAcrossMultipleCalls() {
    String editionSlug = "bhopal-city";
    LocalDate publicationDate = LocalDate.of(2026, 8, 15);
    String fileName = "bhopal-city-2026-08-15.pdf";

    String result1 = S3StorageKeyBuilder.buildKey(editionSlug, publicationDate, fileName);
    String result2 = S3StorageKeyBuilder.buildKey(editionSlug, publicationDate, fileName);
    String result3 = S3StorageKeyBuilder.buildKey(editionSlug, publicationDate, fileName);

    assertEquals(result1, result2);
    assertEquals(result2, result3);
  }
}
