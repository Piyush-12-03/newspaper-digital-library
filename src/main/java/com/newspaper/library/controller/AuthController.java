package com.newspaper.library.controller;

import com.newspaper.library.dto.auth.*;
import com.newspaper.library.dto.common.GenericResponse;
import com.newspaper.library.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication and user management controller.
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "API for authentication and user management")
public class AuthController {

  private final AuthService authService;

  @Operation(
          summary = "User login",
          description = "Authenticate and receive JWT token (1 hour expiry)",
          responses = {
                  @ApiResponse(responseCode = "200", description = "Login successful"),
                  @ApiResponse(responseCode = "400", description = "Invalid request"),
                  @ApiResponse(responseCode = "401", description = "Invalid credentials")
          }
  )
  @PostMapping("/login")
  public GenericResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
    log.info("POST /api/v1/auth/login - User: {}", request.getUsername());
    LoginResponse data = authService.login(request);
    return GenericResponse.success(data, "Login successful");
  }

  @Operation(
          summary = "Register new user",
          description = "Register new user with USER role",
          responses = {
                  @ApiResponse(responseCode = "200", description = "User registered"),
                  @ApiResponse(responseCode = "400", description = "Invalid request"),
                  @ApiResponse(responseCode = "409", description = "Username exists")
          }
  )
  @PostMapping("/register")
  public GenericResponse<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
    log.info("POST /api/v1/auth/register - User: {}", request.getUsername());
    RegisterResponse data = authService.register(request);
    return GenericResponse.success(HttpStatus.CREATED.value(), data, "User registered successfully");
  }

  @Operation(
          summary = "Promote user to ADMIN",
          description = "Promote USER to ADMIN role (Admin only)",
          security = @SecurityRequirement(name = "Bearer Authentication"),
          responses = {
                  @ApiResponse(responseCode = "200", description = "User promoted"),
                  @ApiResponse(responseCode = "400", description = "Already ADMIN"),
                  @ApiResponse(responseCode = "401", description = "Unauthorized"),
                  @ApiResponse(responseCode = "403", description = "Forbidden"),
                  @ApiResponse(responseCode = "404", description = "User not found")
          }
  )
  @PreAuthorize("hasRole('ADMIN')")
  @PostMapping("/promote")
  public GenericResponse<PromoteUserResponse> promoteToAdmin(@Valid @RequestBody PromoteUserRequest request) {
    log.info("POST /api/v1/auth/promote - User ID: {}", request.getUserId());
    PromoteUserResponse data = authService.promoteToAdmin(request);
    return GenericResponse.success(data, "User promoted to ADMIN successfully");
  }
}
