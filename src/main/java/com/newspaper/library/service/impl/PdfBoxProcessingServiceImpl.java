package com.newspaper.library.service.impl;

import com.newspaper.library.dto.storage.PdfMetadata;
import com.newspaper.library.exception.InvalidFileException;
import com.newspaper.library.service.PdfProcessingService;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;

/**
 * Apache PDFBox implementation for PDF processing.
 * <p>
 * PDFBox is a robust, enterprise-grade library that provides:
 * - Page count extraction
 * - Metadata extraction (title, author, subject, etc.)
 * - Text extraction
 * - PDF manipulation (merge, split, etc.)
 * - Image extraction
 * - PDF creation
 * - Digital signatures
 * <p>
 * Future capabilities available without changing dependencies:
 * - Extract text for search/indexing
 * - Generate thumbnails
 * - Extract images
 * - Validate PDF structure
 * - Add watermarks
 */
@Slf4j
@Service
public class PdfBoxProcessingServiceImpl implements PdfProcessingService {

  @Override
  public void extractMetadata(InputStream inputStream) {
    log.debug("Extracting PDF metadata");

    try (PDDocument document = Loader.loadPDF(inputStream.readAllBytes())) {
      int pageCount = document.getNumberOfPages();
      PDDocumentInformation info = document.getDocumentInformation();

      PdfMetadata metadata = PdfMetadata.builder()
              .pageCount(pageCount)
              .title(info.getTitle())
              .author(info.getAuthor())
              .subject(info.getSubject())
              .producer(info.getProducer())
              .encrypted(document.isEncrypted())
              .build();

      log.info("PDF metadata extracted - Pages: {}, Encrypted: {}", pageCount, metadata.getEncrypted());

    } catch (IOException e) {
      log.error("Failed to extract PDF metadata", e);
      throw new InvalidFileException("Failed to process PDF file: " + e.getMessage());
    }
  }

  @Override
  public Integer extractPageCount(InputStream inputStream) {
    log.debug("Extracting PDF page count");

    try (PDDocument document = Loader.loadPDF(inputStream.readAllBytes())) {
      int pageCount = document.getNumberOfPages();
      log.info("PDF page count extracted: {}", pageCount);
      return pageCount;

    } catch (IOException e) {
      log.error("Failed to extract PDF page count", e);
      throw new InvalidFileException("Failed to process PDF file: " + e.getMessage());
    }
  }
}
