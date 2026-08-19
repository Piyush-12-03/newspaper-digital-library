package com.newspaper.library.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for admin login response with JWT token.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Login response with JWT token and user details")
public class LoginResponse {

  @Schema(description = "JWT access token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
  private String token;

  @Schema(description = "Token type", example = "Bearer")
  private String tokenType;

  @Schema(description = "Token expiry time in seconds (3600 = 1 hour)", example = "3600")
  private Long expiresIn;

  @Schema(description = "User ID", example = "1")
  private Long userId;

  @Schema(description = "Username", example = "admin")
  private String username;

  @Schema(description = "User role", example = "ADMIN")
  private String role;
}
