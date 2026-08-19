package com.newspaper.library.entity;

import com.newspaper.library.enums.Role;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.Objects;

/**
 * Entity representing an admin user.
 * Passwords are stored as bcrypt hashes, never in plaintext.
 */
@Entity
@Table(
        name = "admin_users",
        indexes = {
                @Index(name = "idx_admin_user_username", columnList = "username")
        }
)
@Getter
@Setter
public class AdminUser {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 50)
  private String username;

  @Column(name = "password_hash", nullable = false, length = 100)
  private String passwordHash;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private Role role;

  @Column(nullable = false)
  private Boolean enabled = true;

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
    AdminUser adminUser = (AdminUser) o;
    return id != null && Objects.equals(id, adminUser.id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }

  @Override
  public String toString() {
    return "AdminUser{" +
            "id=" + id +
            ", username='" + username + '\'' +
            ", role=" + role +
            ", enabled=" + enabled +
            '}';
  }
}
