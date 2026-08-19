package com.newspaper.library.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for promoting user to admin.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Promote user to admin request")
public class PromoteUserRequest {

  @NotNull(message = "User ID is required")
  @Schema(description = "User ID to promote", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
  private Long userId;
}
