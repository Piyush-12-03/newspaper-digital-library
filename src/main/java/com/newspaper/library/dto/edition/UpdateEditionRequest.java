package com.newspaper.library.dto.edition;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating an existing edition.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Update edition request")
public class UpdateEditionRequest {

  @Size(max = 100, message = "Edition name must not exceed 100 characters")
  @Schema(description = "Edition name", example = "Bhopal City", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  private String name;

  @Size(max = 100, message = "City must not exceed 100 characters")
  @Schema(description = "City", example = "Bhopal", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  private String city;

  @Size(max = 100, message = "State must not exceed 100 characters")
  @Schema(description = "State", example = "Madhya Pradesh", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  private String state;

  @Size(max = 50, message = "Language must not exceed 50 characters")
  @Schema(description = "Language", example = "Hindi", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  private String language;

  @Size(max = 500, message = "Description must not exceed 500 characters")
  @Schema(description = "Description", example = "Daily newspaper for Bhopal city", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  private String description;

  @Schema(description = "Active status", example = "true", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  private Boolean active;
}
