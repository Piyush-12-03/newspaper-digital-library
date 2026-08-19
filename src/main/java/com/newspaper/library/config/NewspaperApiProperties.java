package com.newspaper.library.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration properties for the Newspaper API.
 */
@Data
@Component
@Validated
@ConfigurationProperties(prefix = "newspaper.api")
public class NewspaperApiProperties {

  /**
   * Maximum allowed date range for issue queries (in days).
   */
  @Min(1)
  @Max(365)
  private int maxDateRangeDays = 90;

  /**
   * Default page size for paginated requests.
   */
  @Min(1)
  @Max(100)
  private int defaultPageSize = 20;

  /**
   * Maximum allowed page size.
   */
  @Min(1)
  @Max(500)
  private int maxPageSize = 100;
}
