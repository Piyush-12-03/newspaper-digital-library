package com.newspaper.library.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Entity representing a specific issue of a newspaper edition published on a particular date.
 * Example: Bhopal City - 2026-08-12
 * <p>
 * Each issue stores metadata about the PDF. The actual PDF file is stored in AWS S3.
 */
@Entity
@Table(
        name = "issues",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_edition_publication_date",
                        columnNames = {"edition_id", "publication_date"}
                )
        },
        indexes = {
                @Index(name = "idx_issue_publication_date", columnList = "publication_date"),
                @Index(name = "idx_issue_edition_id", columnList = "edition_id")
        }
)
@Getter
@Setter
public class Issue {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "edition_id", nullable = false, foreignKey = @ForeignKey(name = "fk_issue_edition"))
  private Edition edition;

  @Column(name = "publication_date", nullable = false)
  private LocalDate publicationDate;

  @Column(name = "file_name", nullable = false, length = 255)
  private String fileName;

  @Column(name = "storage_key", nullable = false, unique = true, length = 500)
  private String storageKey;

  @Column(name = "content_type", nullable = false, length = 100)
  private String contentType;

  @Column(name = "file_size", nullable = false)
  private Long fileSize;

  @Column(name = "page_count")
  private Integer pageCount;

  @Column(name = "cover_image_key", length = 500)
  private String coverImageKey;

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
    Issue issue = (Issue) o;
    return id != null && Objects.equals(id, issue.id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }

  @Override
  public String toString() {
    return "Issue{" +
            "id=" + id +
            ", publicationDate=" + publicationDate +
            ", fileName='" + fileName + '\'' +
            ", contentType='" + contentType + '\'' +
            ", fileSize=" + fileSize +
            '}';
  }
}
