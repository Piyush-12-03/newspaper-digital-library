package com.newspaper.library.repository;

import com.newspaper.library.entity.Issue;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Issue entity operations.
 */
@Repository
public interface IssueRepository extends JpaRepository<Issue, Long> {

  /**
   * Find all issues published on a specific date.
   *
   * @param publicationDate the publication date
   * @return list of issues
   */
  List<Issue> findByPublicationDate(LocalDate publicationDate);

  /**
   * Find all issues published on a specific date (paginated).
   */
  Page<Issue> findByPublicationDate(LocalDate publicationDate, Pageable pageable);

  /**
   * Find issues published on a specific date for a specific edition (paginated).
   */
  Page<Issue> findByPublicationDateAndEditionId(LocalDate publicationDate, Long editionId, Pageable pageable);

  /**
   * Find all issues published between two dates (inclusive).
   *
   * @param startDate start date
   * @param endDate   end date
   * @return list of issues
   */
  List<Issue> findByPublicationDateBetween(LocalDate startDate, LocalDate endDate);

  /**
   * Find all issues published between two dates (paginated).
   */
  Page<Issue> findByPublicationDateBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);

  /**
   * Find issues published between two dates for a specific edition (paginated).
   */
  Page<Issue> findByPublicationDateBetweenAndEditionId(LocalDate startDate, LocalDate endDate, Long editionId, Pageable pageable);

  /**
   * Find all issues for a specific edition (paginated).
   */
  Page<Issue> findByEditionId(Long editionId, Pageable pageable);

  /**
   * Find a specific issue by edition ID and publication date.
   * This leverages the unique constraint.
   *
   * @param editionId       edition ID
   * @param publicationDate publication date
   * @return Optional containing the issue if found
   */
  Optional<Issue> findByEditionIdAndPublicationDate(Long editionId, LocalDate publicationDate);

  /**
   * Check if an issue exists for a given edition and publication date.
   *
   * @param editionId       edition ID
   * @param publicationDate publication date
   * @return true if exists, false otherwise
   */
  boolean existsByEditionIdAndPublicationDate(Long editionId, LocalDate publicationDate);

  /**
   * Find all issues for a specific edition ordered by publication date descending.
   *
   * @param editionId edition ID
   * @return list of issues
   */
  List<Issue> findByEditionIdOrderByPublicationDateDesc(Long editionId);

  /**
   * Count issues for a specific edition.
   *
   * @param editionId edition ID
   * @return count of issues
   */
  long countByEditionId(Long editionId);
}
