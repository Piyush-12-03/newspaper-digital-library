package com.newspaper.library.repository;

import com.newspaper.library.entity.AdminUser;
import com.newspaper.library.enums.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for AdminUserRepository.
 */
class AdminUserRepositoryTest extends BaseRepositoryTest {

  @Autowired
  private AdminUserRepository adminUserRepository;

  private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

  @Test
  void shouldSaveValidAdminUser() {
    // Given
    AdminUser admin = createAdminUser("admin1", "password123");

    // When
    AdminUser saved = adminUserRepository.save(admin);

    // Then
    assertThat(saved.getId()).isNotNull();
    assertThat(saved.getUsername()).isEqualTo("admin1");
    assertThat(saved.getPasswordHash()).isNotEqualTo("password123"); // Should be hashed
    assertThat(saved.getPasswordHash()).startsWith("$2a$"); // BCrypt hash format
    assertThat(saved.getRole()).isEqualTo(Role.ADMIN);
    assertThat(saved.getEnabled()).isTrue();
    assertThat(saved.getCreatedAt()).isNotNull();
    assertThat(saved.getUpdatedAt()).isNotNull();
  }

  @Test
  void shouldNeverStorePlaintextPassword() {
    // Given
    String plainPassword = "mySecretPassword123";
    AdminUser admin = createAdminUser("secureAdmin", plainPassword);

    // When
    AdminUser saved = adminUserRepository.save(admin);

    // Then
    assertThat(saved.getPasswordHash()).isNotEqualTo(plainPassword);
    assertThat(passwordEncoder.matches(plainPassword, saved.getPasswordHash())).isTrue();
  }

  @Test
  void shouldFindAdminUserByUsername() {
    // Given
    AdminUser admin = createAdminUser("findme", "password");
    adminUserRepository.save(admin);

    // When
    Optional<AdminUser> found = adminUserRepository.findByUsername("findme");

    // Then
    assertThat(found).isPresent();
    assertThat(found.get().getUsername()).isEqualTo("findme");
  }

  @Test
  void shouldReturnEmptyForNonExistentUsername() {
    // When
    Optional<AdminUser> found = adminUserRepository.findByUsername("nonexistent");

    // Then
    assertThat(found).isEmpty();
  }

  @Test
  void shouldCheckExistenceByUsername() {
    // Given
    AdminUser admin = createAdminUser("exists", "password");
    adminUserRepository.save(admin);

    // When & Then
    assertThat(adminUserRepository.existsByUsername("exists")).isTrue();
    assertThat(adminUserRepository.existsByUsername("doesnotexist")).isFalse();
  }

  @Test
  void shouldEnforceUsernameUniqueness() {
    // Given
    AdminUser admin1 = createAdminUser("duplicate", "password1");
    AdminUser admin2 = createAdminUser("duplicate", "password2");

    adminUserRepository.save(admin1);

    // When & Then - duplicate username should fail
    assertThatThrownBy(() -> {
      adminUserRepository.save(admin2);
      adminUserRepository.flush();
    }).isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  void shouldHandleEnabledFlag() {
    // Given
    AdminUser enabledAdmin = createAdminUser("enabled", "password");
    enabledAdmin.setEnabled(true);

    AdminUser disabledAdmin = createAdminUser("disabled", "password");
    disabledAdmin.setEnabled(false);

    // When
    AdminUser saved1 = adminUserRepository.save(enabledAdmin);
    AdminUser saved2 = adminUserRepository.save(disabledAdmin);

    // Then
    assertThat(saved1.getEnabled()).isTrue();
    assertThat(saved2.getEnabled()).isFalse();
  }

  @Test
  void shouldSupportAdminRole() {
    // Given
    AdminUser admin = createAdminUser("roletest", "password");
    admin.setRole(Role.ADMIN);

    // When
    AdminUser saved = adminUserRepository.save(admin);

    // Then
    assertThat(saved.getRole()).isEqualTo(Role.ADMIN);
  }

  @Test
  void shouldNotIncludePasswordInToString() {
    // Given
    AdminUser admin = createAdminUser("security", "secretPassword123");
    adminUserRepository.save(admin);

    // When
    String toString = admin.toString();

    // Then
    assertThat(toString).doesNotContain("password");
    assertThat(toString).doesNotContain("secretPassword123");
    assertThat(toString).doesNotContain(admin.getPasswordHash());
  }

  private AdminUser createAdminUser(String username, String plainPassword) {
    AdminUser admin = new AdminUser();
    admin.setUsername(username);
    admin.setPasswordHash(passwordEncoder.encode(plainPassword));
    admin.setRole(Role.ADMIN);
    admin.setEnabled(true);
    return admin;
  }
}
