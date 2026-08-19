package com.newspaper.library.service.impl;

import com.newspaper.library.dto.storage.PdfMetadata;
import com.newspaper.library.exception.InvalidFileException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PdfBoxProcessingServiceImpl.
 * Note: These tests use mock/invalid PDFs for unit testing.
 * Real PDF testing would require integration tests with actual PDF files.
 */
class PdfBoxProcessingServiceImplTest {

  private PdfBoxProcessingServiceImpl pdfProcessingService;

  @BeforeEach
  void setUp() {
    pdfProcessingService = new PdfBoxProcessingServiceImpl();
  }

  @Test
  void extractPageCount_withInvalidPdf_shouldThrowInvalidFileException() {
    // Arrange
    byte[] invalidPdfContent = "This is not a PDF file".getBytes();
    InputStream inputStream = new ByteArrayInputStream(invalidPdfContent);

    // Act & Assert
    assertThrows(InvalidFileException.class, () -> pdfProcessingService.extractPageCount(inputStream));
  }

  @Test
  void extractMetadata_withInvalidPdf_shouldThrowInvalidFileException() {
    // Arrange
    byte[] invalidPdfContent = "This is not a PDF file".getBytes();
    InputStream inputStream = new ByteArrayInputStream(invalidPdfContent);

    // Act & Assert
    assertThrows(InvalidFileException.class, () -> pdfProcessingService.extractMetadata(inputStream));
  }

  @Test
  void extractPageCount_withEmptyStream_shouldThrowInvalidFileException() {
    // Arrange
    InputStream inputStream = new ByteArrayInputStream(new byte[0]);

    // Act & Assert
    assertThrows(InvalidFileException.class, () -> pdfProcessingService.extractPageCount(inputStream));
  }

  @Test
  void extractMetadata_withEmptyStream_shouldThrowInvalidFileException() {
    // Arrange
    InputStream inputStream = new ByteArrayInputStream(new byte[0]);

    // Act & Assert
    assertThrows(InvalidFileException.class, () -> pdfProcessingService.extractMetadata(inputStream));
  }
}
