package com.newspaper.library.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for user promotion.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User promotion response")
public class PromoteUserResponse {

  @Schema(description = "User ID", example = "2")
  private Long userId;

  @Schema(description = "Username", example = "john_doe")
  private String username;

  @Schema(description = "Previous role", example = "USER")
  private String previousRole;

  @Schema(description = "New role", example = "ADMIN")
  private String newRole;

  @Schema(description = "Success message", example = "User promoted to ADMIN successfully")
  private String message;
}
