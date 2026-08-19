package com.newspaper.library.dto.storage;

import lombok.Builder;
import lombok.Value;

/**
 * PDF metadata extracted from uploaded file.
 */
@Value
@Builder
public class PdfMetadata {

  /**
   * Number of pages in the PDF
   */
  Integer pageCount;

  /**
   * PDF title (from metadata)
   */
  String title;

  /**
   * PDF author (from metadata)
   */
  String author;

  /**
   * PDF subject (from metadata)
   */
  String subject;

  /**
   * PDF producer (software used to create)
   */
  String producer;

  /**
   * Whether the PDF is encrypted
   */
  Boolean encrypted;
}
