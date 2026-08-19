package com.newspaper.library.repository;

import com.newspaper.library.entity.Edition;
import com.newspaper.library.enums.EditionType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for EditionRepository.
 */
class EditionRepositoryTest extends BaseRepositoryTest {

  @Autowired
  private EditionRepository editionRepository;

  @Test
  void shouldSaveValidEdition() {
    // Given
    Edition edition = createEdition("Bhopal City", "bhopal-city", "Bhopal", EditionType.CITY);

    // When
    Edition saved = editionRepository.save(edition);

    // Then
    assertThat(saved.getId()).isNotNull();
    assertThat(saved.getName()).isEqualTo("Bhopal City");
    assertThat(saved.getSlug()).isEqualTo("bhopal-city");
    assertThat(saved.getCity()).isEqualTo("Bhopal");
    assertThat(saved.getEditionType()).isEqualTo(EditionType.CITY);
    assertThat(saved.getActive()).isTrue();
    assertThat(saved.getCreatedAt()).isNotNull();
    assertThat(saved.getUpdatedAt()).isNotNull();
  }

  @Test
  void shouldFindEditionBySlug() {
    // Given
    Edition edition = createEdition("Indore City", "indore-city", "Indore", EditionType.CITY);
    editionRepository.save(edition);

    // When
    Optional<Edition> found = editionRepository.findBySlug("indore-city");

    // Then
    assertThat(found).isPresent();
    assertThat(found.get().getName()).isEqualTo("Indore City");
  }

  @Test
  void shouldReturnEmptyForNonExistentSlug() {
    // When
    Optional<Edition> found = editionRepository.findBySlug("non-existent");

    // Then
    assertThat(found).isEmpty();
  }

  @Test
  void shouldCheckExistenceBySlug() {
    // Given
    Edition edition = createEdition("Jabalpur", "jabalpur", "Jabalpur", EditionType.CITY);
    editionRepository.save(edition);

    // When & Then
    assertThat(editionRepository.existsBySlug("jabalpur")).isTrue();
    assertThat(editionRepository.existsBySlug("non-existent")).isFalse();
  }

  @Test
  void shouldEnforceSlugUniqueness() {
    // Given
    Edition edition1 = createEdition("Gwalior City", "gwalior", "Gwalior", EditionType.CITY);
    Edition edition2 = createEdition("Gwalior Region", "gwalior", "Gwalior", EditionType.REGION);

    editionRepository.save(edition1);

    // When & Then - attempting to save duplicate slug should fail
    assertThatThrownBy(() -> {
      editionRepository.save(edition2);
      editionRepository.flush();
    }).isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  void shouldAllowSameNameWithDifferentSlug() {
    // Given
    Edition edition1 = createEdition("City Edition", "city-edition-1", "Mumbai", EditionType.CITY);
    Edition edition2 = createEdition("City Edition", "city-edition-2", "Delhi", EditionType.CITY);

    // When
    editionRepository.save(edition1);
    editionRepository.save(edition2);
    editionRepository.flush();

    // Then - should succeed as slugs are different
    assertThat(editionRepository.count()).isEqualTo(2);
  }

  @Test
  void shouldHandleNullableRegion() {
    // Given
    Edition edition = createEdition("Sagar City", "sagar-city", "Sagar", EditionType.CITY);
    edition.setRegion(null);

    // When
    Edition saved = editionRepository.save(edition);

    // Then
    assertThat(saved.getRegion()).isNull();
  }

  @Test
  void shouldSupportDifferentEditionTypes() {
    // Given & When
    Edition city = createEdition("City Edition", "city-ed", "Mumbai", EditionType.CITY);
    Edition upcountry = createEdition("Upcountry Edition", "upcountry-ed", "Ratlam", EditionType.UPCOUNTRY);
    Edition region = createEdition("Region Edition", "region-ed", "Bhopal", EditionType.REGION);
    Edition other = createEdition("Special Edition", "special-ed", "Delhi", EditionType.OTHER);

    editionRepository.save(city);
    editionRepository.save(upcountry);
    editionRepository.save(region);
    editionRepository.save(other);

    // Then
    assertThat(editionRepository.count()).isEqualTo(4);
  }

  private Edition createEdition(String name, String slug, String city, EditionType type) {
    Edition edition = new Edition();
    edition.setName(name);
    edition.setSlug(slug);
    edition.setCity(city);
    edition.setRegion("Madhya Pradesh");
    edition.setEditionType(type);
    edition.setActive(true);
    return edition;
  }
}
