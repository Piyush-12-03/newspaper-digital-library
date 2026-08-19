package com.newspaper.library.dto.edition;

import com.newspaper.library.enums.EditionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * DTO for edition response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response containing edition details")
public class EditionResponse {

  @Schema(description = "Unique identifier of the edition", example = "1")
  private Long id;

  @Schema(description = "Name of the edition", example = "Bhopal City")
  private String name;

  @Schema(description = "URL-friendly slug", example = "bhopal-city")
  private String slug;

  @Schema(description = "City where the edition is published", example = "Bhopal")
  private String city;

  @Schema(description = "Region of the edition", example = "Madhya Pradesh")
  private String region;

  @Schema(description = "Type of edition", example = "CITY")
  private EditionType editionType;

  @Schema(description = "Whether the edition is active", example = "true")
  private Boolean active;

  @Schema(description = "Timestamp when the edition was created")
  private Instant createdAt;

  @Schema(description = "Timestamp when the edition was last updated")
  private Instant updatedAt;
}
