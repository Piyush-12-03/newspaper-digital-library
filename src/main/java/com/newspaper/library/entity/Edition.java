package com.newspaper.library.entity;

import com.newspaper.library.enums.EditionType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.Objects;

/**
 * Entity representing a newspaper edition (e.g., Bhopal City, Indore City).
 * An edition can have multiple issues published on different dates.
 */
@Entity
@Table(
        name = "editions",
        indexes = {
                @Index(name = "idx_edition_slug", columnList = "slug"),
                @Index(name = "idx_edition_active", columnList = "active")
        }
)
@Getter
@Setter
public class Edition {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 100)
  private String name;

  @Column(nullable = false, unique = true, length = 100)
  private String slug;

  @Column(nullable = false, length = 100)
  private String city;

  @Column(length = 100)
  private String state;

  @Column(length = 100)
  private String region;

  @Column(length = 50)
  private String language;

  @Column(length = 500)
  private String description;

  @Enumerated(EnumType.STRING)
  @Column(name = "edition_type", nullable = false, length = 20)
  private EditionType editionType;

  @Column(nullable = false)
  private Boolean active = true;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @PrePersist
  protected void onCreate() {
    Instant now = Instant.now();
    createdAt = now;
    updatedAt = now;
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = Instant.now();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Edition edition = (Edition) o;
    return id != null && Objects.equals(id, edition.id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }

  @Override
  public String toString() {
    return "Edition{" +
            "id=" + id +
            ", name='" + name + '\'' +
            ", slug='" + slug + '\'' +
            ", city='" + city + '\'' +
            ", editionType=" + editionType +
            ", active=" + active +
            '}';
  }
}
