package com.newspaper.library.repository;

import com.newspaper.library.entity.Edition;
import com.newspaper.library.entity.Issue;
import com.newspaper.library.enums.EditionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for IssueRepository.
 */
class IssueRepositoryTest extends BaseRepositoryTest {

  @Autowired
  private IssueRepository issueRepository;

  @Autowired
  private EditionRepository editionRepository;

  private Edition bhopałEdition;
  private Edition indoreEdition;

  @BeforeEach
  void setUp() {
    bhopałEdition = createAndSaveEdition("Bhopal City", "bhopal-city", "Bhopal");
    indoreEdition = createAndSaveEdition("Indore City", "indore-city", "Indore");
  }

  @Test
  void shouldSaveValidIssue() {
    // Given
    LocalDate publicationDate = LocalDate.of(2026, 8, 14);
    Issue issue = createIssue(bhopałEdition, publicationDate, "bhopal-2026-08-14.pdf");

    // When
    Issue saved = issueRepository.save(issue);

    // Then
    assertThat(saved.getId()).isNotNull();
    assertThat(saved.getEdition()).isEqualTo(bhopałEdition);
    assertThat(saved.getPublicationDate()).isEqualTo(publicationDate);
    assertThat(saved.getFileName()).isEqualTo("bhopal-2026-08-14.pdf");
    assertThat(saved.getStorageKey()).contains("bhopal-2026-08-14");
    assertThat(saved.getContentType()).isEqualTo("application/pdf");
    assertThat(saved.getFileSize()).isEqualTo(1024000L);
    assertThat(saved.getCreatedAt()).isNotNull();
    assertThat(saved.getUpdatedAt()).isNotNull();
  }

  @Test
  void shouldEnforceEditionDateUniqueness() {
    // Given
    LocalDate date = LocalDate.of(2026, 8, 14);
    Issue issue1 = createIssue(bhopałEdition, date, "file1.pdf");
    Issue issue2 = createIssue(bhopałEdition, date, "file2.pdf");

    issueRepository.save(issue1);

    // When & Then - duplicate edition + date should fail
    assertThatThrownBy(() -> {
      issueRepository.save(issue2);
      issueRepository.flush();
    }).isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  void shouldAllowSameDateForDifferentEditions() {
    // Given
    LocalDate date = LocalDate.of(2026, 8, 14);
    Issue bhopałIssue = createIssue(bhopałEdition, date, "bhopal.pdf");
    Issue indoreIssue = createIssue(indoreEdition, date, "indore.pdf");

    // When
    issueRepository.save(bhopałIssue);
    issueRepository.save(indoreIssue);
    issueRepository.flush();

    // Then - should succeed as editions are different
    assertThat(issueRepository.count()).isEqualTo(2);
  }

  @Test
  void shouldAllowSameEditionForDifferentDates() {
    // Given
    Issue issue1 = createIssue(bhopałEdition, LocalDate.of(2026, 8, 13), "file1.pdf");
    Issue issue2 = createIssue(bhopałEdition, LocalDate.of(2026, 8, 14), "file2.pdf");
    Issue issue3 = createIssue(bhopałEdition, LocalDate.of(2026, 8, 15), "file3.pdf");

    // When
    issueRepository.save(issue1);
    issueRepository.save(issue2);
    issueRepository.save(issue3);

    // Then
    assertThat(issueRepository.count()).isEqualTo(3);
  }

  @Test
  void shouldFindIssuesByPublicationDate() {
    // Given
    LocalDate targetDate = LocalDate.of(2026, 8, 14);
    issueRepository.save(createIssue(bhopałEdition, targetDate, "bhopal.pdf"));
    issueRepository.save(createIssue(indoreEdition, targetDate, "indore.pdf"));
    issueRepository.save(createIssue(bhopałEdition, LocalDate.of(2026, 8, 15), "other.pdf"));

    // When
    List<Issue> issues = issueRepository.findByPublicationDate(targetDate);

    // Then
    assertThat(issues).hasSize(2);
    assertThat(issues).allMatch(issue -> issue.getPublicationDate().equals(targetDate));
  }

  @Test
  void shouldFindIssuesByDateRange() {
    // Given
    issueRepository.save(createIssue(bhopałEdition, LocalDate.of(2026, 8, 12), "file1.pdf"));
    issueRepository.save(createIssue(bhopałEdition, LocalDate.of(2026, 8, 13), "file2.pdf"));
    issueRepository.save(createIssue(bhopałEdition, LocalDate.of(2026, 8, 14), "file3.pdf"));
    issueRepository.save(createIssue(bhopałEdition, LocalDate.of(2026, 8, 15), "file4.pdf"));
    issueRepository.save(createIssue(bhopałEdition, LocalDate.of(2026, 8, 16), "file5.pdf"));

    // When
    LocalDate startDate = LocalDate.of(2026, 8, 13);
    LocalDate endDate = LocalDate.of(2026, 8, 15);
    List<Issue> issues = issueRepository.findByPublicationDateBetween(startDate, endDate);

    // Then
    assertThat(issues).hasSize(3);
    assertThat(issues).allMatch(issue ->
            !issue.getPublicationDate().isBefore(startDate) &&
                    !issue.getPublicationDate().isAfter(endDate)
    );
  }

  @Test
  void shouldFindIssueByEditionAndDate() {
    // Given
    LocalDate date = LocalDate.of(2026, 8, 14);
    issueRepository.save(createIssue(bhopałEdition, date, "bhopal.pdf"));
    issueRepository.save(createIssue(indoreEdition, date, "indore.pdf"));

    // When
    Optional<Issue> found = issueRepository.findByEditionIdAndPublicationDate(
            bhopałEdition.getId(), date
    );

    // Then
    assertThat(found).isPresent();
    assertThat(found.get().getEdition().getId()).isEqualTo(bhopałEdition.getId());
    assertThat(found.get().getPublicationDate()).isEqualTo(date);
  }

  @Test
  void shouldCheckExistenceByEditionAndDate() {
    // Given
    LocalDate date = LocalDate.of(2026, 8, 14);
    issueRepository.save(createIssue(bhopałEdition, date, "bhopal.pdf"));

    // When & Then
    assertThat(issueRepository.existsByEditionIdAndPublicationDate(
            bhopałEdition.getId(), date
    )).isTrue();

    assertThat(issueRepository.existsByEditionIdAndPublicationDate(
            bhopałEdition.getId(), LocalDate.of(2026, 8, 15)
    )).isFalse();

    assertThat(issueRepository.existsByEditionIdAndPublicationDate(
            indoreEdition.getId(), date
    )).isFalse();
  }

  @Test
  void shouldFindIssuesByEditionOrderedByDateDesc() {
    // Given
    issueRepository.save(createIssue(bhopałEdition, LocalDate.of(2026, 8, 12), "file1.pdf"));
    issueRepository.save(createIssue(bhopałEdition, LocalDate.of(2026, 8, 15), "file2.pdf"));
    issueRepository.save(createIssue(bhopałEdition, LocalDate.of(2026, 8, 13), "file3.pdf"));
    issueRepository.save(createIssue(indoreEdition, LocalDate.of(2026, 8, 14), "other.pdf"));

    // When
    List<Issue> issues = issueRepository.findByEditionIdOrderByPublicationDateDesc(bhopałEdition.getId());

    // Then
    assertThat(issues).hasSize(3);
    assertThat(issues.get(0).getPublicationDate()).isEqualTo(LocalDate.of(2026, 8, 15));
    assertThat(issues.get(1).getPublicationDate()).isEqualTo(LocalDate.of(2026, 8, 13));
    assertThat(issues.get(2).getPublicationDate()).isEqualTo(LocalDate.of(2026, 8, 12));
  }

  @Test
  void shouldHandleNullablePageCount() {
    // Given
    Issue issue = createIssue(bhopałEdition, LocalDate.of(2026, 8, 14), "file.pdf");
    issue.setPageCount(null);

    // When
    Issue saved = issueRepository.save(issue);

    // Then
    assertThat(saved.getPageCount()).isNull();
  }

  @Test
  void shouldHandleNullableCoverImageKey() {
    // Given
    Issue issue = createIssue(bhopałEdition, LocalDate.of(2026, 8, 14), "file.pdf");
    issue.setCoverImageKey(null);

    // When
    Issue saved = issueRepository.save(issue);

    // Then
    assertThat(saved.getCoverImageKey()).isNull();
  }

  @Test
  void shouldCascadeDeleteWhenEditionIsDeleted() {
    // Given
    Edition tempEdition = createAndSaveEdition("Temp Edition", "temp-edition", "TempCity");
    issueRepository.save(createIssue(tempEdition, LocalDate.of(2026, 8, 14), "file.pdf"));

    Long editionId = tempEdition.getId();
    assertThat(issueRepository.findByEditionIdOrderByPublicationDateDesc(editionId)).hasSize(1);

    // When
    editionRepository.delete(tempEdition);
    editionRepository.flush();

    // Then
    assertThat(issueRepository.findByEditionIdOrderByPublicationDateDesc(editionId)).isEmpty();
  }

  private Edition createAndSaveEdition(String name, String slug, String city) {
    Edition edition = new Edition();
    edition.setName(name);
    edition.setSlug(slug);
    edition.setCity(city);
    edition.setEditionType(EditionType.CITY);
    edition.setActive(true);
    return editionRepository.save(edition);
  }

  private Issue createIssue(Edition edition, LocalDate publicationDate, String fileName) {
    Issue issue = new Issue();
    issue.setEdition(edition);
    issue.setPublicationDate(publicationDate);
    issue.setFileName(fileName);
    issue.setStorageKey("s3://bucket/newspapers/" + edition.getSlug() + "/" +
            publicationDate + "/" + fileName);
    issue.setContentType("application/pdf");
    issue.setFileSize(1024000L);
    issue.setPageCount(12);
    issue.setCoverImageKey("s3://bucket/covers/" + edition.getSlug() + "/" +
            publicationDate + "/cover.jpg");
    return issue;
  }
}
