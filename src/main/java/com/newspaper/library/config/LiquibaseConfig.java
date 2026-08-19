package com.newspaper.library.config;

import liquibase.integration.spring.SpringLiquibase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * Liquibase configuration to ensure migrations run before Hibernate validation.
 * This prevents "missing column" errors when adding new fields via Liquibase.
 * <p>
 * Order of execution:
 * 1. Database connection established
 * 2. Liquibase migrations run (adds/modifies columns)
 * 3. Hibernate initializes and validates schema
 * 4. Application starts
 */
@Configuration
public class LiquibaseConfig {

  @Value("${spring.liquibase.change-log:classpath:db/changelog/db.changelog-master.xml}")
  private String changeLog;

  @Value("${spring.liquibase.enabled:true}")
  private boolean enabled;

  /**
   * Configure Liquibase bean with explicit dependency on DataSource.
   * This ensures Liquibase runs immediately after DataSource is ready.
   * <p>
   * The EntityManagerFactory will automatically depend on this bean due to
   * Spring Boot's EntityManagerFactoryDependsOnPostProcessor.
   */
  @Bean
  public SpringLiquibase liquibase(DataSource dataSource) {
    SpringLiquibase liquibase = new SpringLiquibase();
    liquibase.setDataSource(dataSource);
    liquibase.setChangeLog(changeLog);
    liquibase.setShouldRun(enabled);

    return liquibase;
  }
}
