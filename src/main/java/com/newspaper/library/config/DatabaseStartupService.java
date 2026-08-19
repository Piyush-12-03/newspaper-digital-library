package com.newspaper.library.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * Database startup service for creating performance indexes.
 * Runs once on application startup to ensure optimal query performance.
 * <p>
 * Note: Schema and seed data are managed by Liquibase.
 * This service only handles runtime index optimizations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DatabaseStartupService {

  private final JdbcTemplate jdbcTemplate;

  @PostConstruct
  public void createIndexes() {
    log.info("Checking and creating database indexes for optimal performance...");

    try {
      // Indexes for editions table
      createIndexIfNotExists(
              "idx_editions_slug",
              "editions",
              "slug",
              "Optimizes edition lookup by slug"
      );

      createIndexIfNotExists(
              "idx_editions_city",
              "editions",
              "city",
              "Optimizes edition filtering by city"
      );

      createIndexIfNotExists(
              "idx_editions_active",
              "editions",
              "active",
              "Optimizes active edition queries"
      );

      // Indexes for issues table
      createIndexIfNotExists(
              "idx_issues_publication_date",
              "issues",
              "publication_date",
              "Optimizes issue queries by date"
      );

      createIndexIfNotExists(
              "idx_issues_edition_id",
              "issues",
              "edition_id",
              "Optimizes issue queries by edition"
      );

      createIndexIfNotExists(
              "idx_issues_edition_date",
              "issues",
              "edition_id, publication_date",
              "Optimizes combined edition and date queries"
      );

      // Indexes for admin_users table
      createIndexIfNotExists(
              "idx_admin_users_username",
              "admin_users",
              "username",
              "Optimizes authentication queries"
      );

      log.info("Database indexes verified successfully");

    } catch (Exception e) {
      log.error("Error creating indexes: {}", e.getMessage(), e);
      // Don't throw - allow application to start even if index creation fails
    }
  }

  /**
   * Creates an index if it doesn't already exist.
   * PostgreSQL-specific implementation using CREATE INDEX IF NOT EXISTS.
   */
  private void createIndexIfNotExists(String indexName, String tableName, String columns, String description) {
    try {
      String sql = String.format(
              "CREATE INDEX IF NOT EXISTS %s ON %s (%s)",
              indexName, tableName, columns
      );

      jdbcTemplate.execute(sql);
      log.debug("Index '{}' on {} ({}): {}", indexName, tableName, columns, description);

    } catch (Exception e) {
      log.warn("Failed to create index '{}': {}", indexName, e.getMessage());
    }
  }
}
