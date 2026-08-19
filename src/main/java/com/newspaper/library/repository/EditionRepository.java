package com.newspaper.library.repository;

import com.newspaper.library.entity.Edition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Edition entity operations.
 */
@Repository
public interface EditionRepository extends JpaRepository<Edition, Long> {

  /**
   * Find an edition by its slug.
   *
   * @param slug the edition slug
   * @return Optional containing the edition if found
   */
  Optional<Edition> findBySlug(String slug);

  /**
   * Find an edition by its name (case-insensitive).
   *
   * @param name the edition name
   * @return Optional containing the edition if found
   */
  Optional<Edition> findByNameIgnoreCase(String name);

  /**
   * Find all active editions.
   *
   * @return list of active editions
   */
  List<Edition> findAllByActiveTrue();

  /**
   * Check if an edition exists with the given slug.
   *
   * @param slug the edition slug
   * @return true if exists, false otherwise
   */
  boolean existsBySlug(String slug);
}
