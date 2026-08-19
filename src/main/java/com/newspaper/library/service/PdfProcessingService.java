package com.newspaper.library.service;

import java.io.InputStream;

/**
 * Service for processing PDF files.
 * Provides utilities for extracting metadata, page count, text, etc.
 */
public interface PdfProcessingService {

  /**
   * Extract metadata from PDF file.
   *
   * @param inputStream PDF file input stream
   * @throws com.newspaper.library.exception.InvalidFileException if PDF is invalid or corrupted
   */
  void extractMetadata(InputStream inputStream);

  /**
   * Extract only page count from PDF (lightweight operation).
   *
   * @param inputStream PDF file input stream
   * @return Number of pages in the PDF
   * @throws com.newspaper.library.exception.InvalidFileException if PDF is invalid or corrupted
   */
  Integer extractPageCount(InputStream inputStream);
}
