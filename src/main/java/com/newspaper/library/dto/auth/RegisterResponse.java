package com.newspaper.library.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Response DTO for user registration.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "User registration response")
public class RegisterResponse {

  @Schema(description = "User ID", example = "2")
  private Long userId;

  @Schema(description = "Username", example = "john_doe")
  private String username;

  @Schema(description = "User role", example = "USER")
  private String role;

  @Schema(description = "Account enabled status", example = "true")
  private Boolean enabled;

  @Schema(description = "Registration timestamp")
  private Instant createdAt;

  @Schema(description = "Success message", example = "User registered successfully")
  private String message;
}
