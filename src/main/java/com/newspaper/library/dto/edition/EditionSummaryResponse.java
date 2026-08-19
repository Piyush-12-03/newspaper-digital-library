package com.newspaper.library.dto.edition;

import com.newspaper.library.enums.EditionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Lightweight edition summary for nested responses.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Edition summary information")
public class EditionSummaryResponse {

  @Schema(description = "Unique identifier of the edition", example = "1")
  private Long id;

  @Schema(description = "Name of the edition", example = "Bhopal City")
  private String name;

  @Schema(description = "URL-friendly slug", example = "bhopal-city")
  private String slug;

  @Schema(description = "City where the edition is published", example = "Bhopal")
  private String city;

  @Schema(description = "Type of edition", example = "CITY")
  private EditionType editionType;
}
