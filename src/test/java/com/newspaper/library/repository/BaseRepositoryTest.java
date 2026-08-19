package com.newspaper.library.repository;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.transaction.annotation.Transactional;

/**
 * Base class for repository integration tests using Testcontainers.
 * Provides a real PostgreSQL database for testing.
 */
@SpringBootTest
@Testcontainers
@Transactional
@ActiveProfiles("test")
public abstract class BaseRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("newspaper_library_test")
            .withUsername("test")
            .withPassword("test");
}
