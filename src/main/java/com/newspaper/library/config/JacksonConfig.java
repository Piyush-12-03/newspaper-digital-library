package com.newspaper.library.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Jackson ObjectMapper configuration.
 * Separated from SecurityConfig to avoid circular dependency.
 */
@Configuration
public class JacksonConfig {

  /**
   * ObjectMapper bean for JSON serialization/deserialization.
   * Used by JwtAuthenticationEntryPoint and other components.
   */
  @Bean
  public ObjectMapper objectMapper() {
    return new ObjectMapper();
  }
}
