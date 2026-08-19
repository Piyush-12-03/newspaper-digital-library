package com.newspaper.library.service;

import com.newspaper.library.dto.auth.*;

/**
 * Service interface for authentication and user management operations.
 */
public interface AuthService {

  /**
   * Authenticate user and generate JWT token.
   *
   * @param request login credentials
   * @return login response with JWT token
   */
  LoginResponse login(LoginRequest request);

  /**
   * Register new user with USER role.
   *
   * @param request registration details
   * @return registration response with user details
   */
  RegisterResponse register(RegisterRequest request);

  /**
   * Promote user from USER to ADMIN role (admin only).
   *
   * @param request user ID to promote
   * @return promotion response with updated role
   */
  PromoteUserResponse promoteToAdmin(PromoteUserRequest request);
}
