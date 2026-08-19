package com.newspaper.library.service;

import com.newspaper.library.dto.edition.CreateEditionRequest;
import com.newspaper.library.dto.edition.EditionListResponse;
import com.newspaper.library.dto.edition.EditionResponse;
import com.newspaper.library.dto.edition.UpdateEditionRequest;

/**
 * Service interface for edition and issue management.
 */
public interface EditionService {

  /**
   * Create new edition (Admin only).
   */
  EditionResponse createEdition(CreateEditionRequest request);

  /**
   * Update existing edition (Admin only).
   */
  EditionResponse updateEdition(Long id, UpdateEditionRequest request);

  /**
   * Delete edition (Admin only).
   */
  void deleteEdition(Long id);

  /**
   * Get all editions.
   */
  EditionListResponse getAllEditions();

  /**
   * Get edition by ID.
   */
  EditionResponse getEditionById(Long id);

  /**
   * Get edition by slug.
   */
  EditionResponse getEditionBySlug(String slug);
}
