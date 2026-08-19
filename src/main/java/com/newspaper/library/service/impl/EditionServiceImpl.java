package com.newspaper.library.service.impl;

import com.newspaper.library.dto.edition.CreateEditionRequest;
import com.newspaper.library.dto.edition.EditionListResponse;
import com.newspaper.library.dto.edition.EditionResponse;
import com.newspaper.library.dto.edition.UpdateEditionRequest;
import com.newspaper.library.entity.Edition;
import com.newspaper.library.enums.EditionType;
import com.newspaper.library.exception.DuplicateResourceException;
import com.newspaper.library.exception.ResourceNotFoundException;
import com.newspaper.library.mapper.EditionMapper;
import com.newspaper.library.repository.EditionRepository;
import com.newspaper.library.repository.IssueRepository;
import com.newspaper.library.service.EditionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of edition and issue management service.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EditionServiceImpl implements EditionService {

  private final EditionRepository editionRepository;
  private final IssueRepository issueRepository;
  private final EditionMapper editionMapper;

  @Override
  @Transactional
  public EditionResponse createEdition(CreateEditionRequest request) {
    log.debug("Creating new edition: {}", request.getName());

    // Generate slug from name
    String slug = generateSlug(request.getName());

    // Check if slug already exists
    if (editionRepository.findBySlug(slug).isPresent()) {
      throw new DuplicateResourceException("Edition with name '" + request.getName() + "' already exists");
    }

    // Create edition entity
    Edition edition = new Edition();
    edition.setName(request.getName());
    edition.setSlug(slug);
    edition.setCity(request.getCity());
    edition.setState(request.getState());
    edition.setLanguage(request.getLanguage());
    edition.setDescription(request.getDescription());
    edition.setEditionType(EditionType.CITY); // Default to CITY type
    edition.setActive(true);

    Edition savedEdition = editionRepository.save(edition);
    log.info("Edition created successfully: {} (id={})", savedEdition.getName(), savedEdition.getId());

    return editionMapper.toResponse(savedEdition);
  }

  @Override
  @Transactional
  public EditionResponse updateEdition(Long id, UpdateEditionRequest request) {
    log.debug("Updating edition id: {}", id);

    Edition edition = editionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Edition not found with id: " + id));

    // Update only provided fields
    boolean updated = false;

    if (request.getName() != null && !request.getName().equals(edition.getName())) {
      String newSlug = generateSlug(request.getName());
      // Check if new slug conflicts with existing edition
      editionRepository.findBySlug(newSlug).ifPresent(existing -> {
        if (!existing.getId().equals(id)) {
          throw new DuplicateResourceException("Edition with name '" + request.getName() + "' already exists");
        }
      });
      edition.setName(request.getName());
      edition.setSlug(newSlug);
      updated = true;
    }

    if (request.getCity() != null) {
      edition.setCity(request.getCity());
      updated = true;
    }

    if (request.getState() != null) {
      edition.setState(request.getState());
      updated = true;
    }

    if (request.getLanguage() != null) {
      edition.setLanguage(request.getLanguage());
      updated = true;
    }

    if (request.getDescription() != null) {
      edition.setDescription(request.getDescription());
      updated = true;
    }

    if (request.getActive() != null) {
      edition.setActive(request.getActive());
      updated = true;
    }

    if (updated) {
      Edition savedEdition = editionRepository.save(edition);
      log.info("Edition updated successfully: {} (id={})", savedEdition.getName(), savedEdition.getId());
      return editionMapper.toResponse(savedEdition);
    }

    log.info("No changes to update for edition id: {}", id);
    return editionMapper.toResponse(edition);
  }

  @Override
  @Transactional
  public void deleteEdition(Long id) {
    log.debug("Deleting edition id: {}", id);

    Edition edition = editionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Edition not found with id: " + id));

    // Check if edition has issues
    long issueCount = issueRepository.countByEditionId(id);
    if (issueCount > 0) {
      // Soft delete - just mark as inactive
      edition.setActive(false);
      editionRepository.save(edition);
      log.info("Edition marked as inactive (has {} issues): {} (id={})", issueCount, edition.getName(), id);
    } else {
      // Hard delete - no issues
      editionRepository.delete(edition);
      log.info("Edition deleted successfully: {} (id={})", edition.getName(), id);
    }
  }

  /**
   * Generate URL-friendly slug from name.
   */
  private String generateSlug(String name) {
    return name.toLowerCase()
            .trim()
            .replaceAll("[^a-z0-9\\s-]", "")  // Remove special characters
            .replaceAll("\\s+", "-")          // Replace spaces with hyphens
            .replaceAll("-+", "-");           // Replace multiple hyphens with single
  }

  @Override
  @Transactional(readOnly = true)
  public EditionListResponse getAllEditions() {
    log.debug("Fetching all active editions");
    List<Edition> editions = editionRepository.findAllByActiveTrue();
    List<EditionResponse> editionResponses = editions.stream()
            .map(editionMapper::toResponse)
            .toList();

    log.info("Found {} active editions", editionResponses.size());
    return new EditionListResponse(editionResponses, editionResponses.size());
  }

  @Override
  @Transactional(readOnly = true)
  public EditionResponse getEditionById(Long id) {
    log.debug("Fetching edition with id: {}", id);
    Edition edition = editionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Edition not found with id: " + id));

    log.info("Found edition: {}", edition.getName());
    return editionMapper.toResponse(edition);
  }

  @Override
  @Transactional(readOnly = true)
  public EditionResponse getEditionBySlug(String slug) {
    log.debug("Fetching edition with slug: {}", slug);
    Edition edition = editionRepository.findBySlug(slug)
            .orElseThrow(() -> new ResourceNotFoundException("Edition not found with slug: " + slug));

    log.info("Found edition: {}", edition.getName());
    return editionMapper.toResponse(edition);
  }
}
