package com.newspaper.library.service.impl;

import com.newspaper.library.dto.auth.*;
import com.newspaper.library.entity.AdminUser;
import com.newspaper.library.enums.Role;
import com.newspaper.library.exception.DuplicateResourceException;
import com.newspaper.library.exception.InvalidRequestException;
import com.newspaper.library.exception.ResourceNotFoundException;
import com.newspaper.library.repository.AdminUserRepository;
import com.newspaper.library.service.AuthService;
import com.newspaper.library.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of authentication service.
 * Handles user login, registration, and role management.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

  private final AdminUserRepository adminUserRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtUtil jwtUtil;

  @Override
  @Transactional(readOnly = true)
  public LoginResponse login(LoginRequest request) {
    log.debug("Attempting login for username: {}", request.getUsername());

    // Find user
    AdminUser user = adminUserRepository.findByUsername(request.getUsername())
            .orElseThrow(() -> new ResourceNotFoundException("Invalid username or password"));

    // Check if user is enabled
    if (Boolean.FALSE.equals(user.getEnabled())) {
      log.warn("Login attempt for disabled user: {}", request.getUsername());
      throw new BadCredentialsException("Account is disabled");
    }

    // Verify password
    if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
      log.warn("Invalid password attempt for username: {}", request.getUsername());
      throw new BadCredentialsException("Invalid username or password");
    }

    // Generate JWT token
    String token = jwtUtil.generateToken(
            user.getId(),
            user.getUsername(),
            user.getRole().name()
    );

    log.info("Login successful for user: {} (role: {})", request.getUsername(), user.getRole());

    return LoginResponse.builder()
            .token(token)
            .expiresIn(3600L) // 1 hour in seconds
            .tokenType("Bearer")
            .userId(user.getId())
            .username(user.getUsername())
            .role(user.getRole().name())
            .build();
  }

  @Override
  @Transactional
  public RegisterResponse register(RegisterRequest request) {
    log.debug("Attempting to register user: {}", request.getUsername());

    // Check if username already exists
    if (adminUserRepository.findByUsername(request.getUsername()).isPresent()) {
      throw new DuplicateResourceException("Username already exists: " + request.getUsername());
    }

    // Create new user with USER role
    AdminUser newUser = new AdminUser();
    newUser.setUsername(request.getUsername());
    newUser.setPasswordHash(passwordEncoder.encode(request.getPassword()));
    newUser.setRole(Role.USER);
    newUser.setEnabled(true);

    AdminUser savedUser = adminUserRepository.save(newUser);

    log.info("User registered successfully: {} with role USER", savedUser.getUsername());

    return RegisterResponse.builder()
            .userId(savedUser.getId())
            .username(savedUser.getUsername())
            .role(savedUser.getRole().name())
            .enabled(savedUser.getEnabled())
            .createdAt(savedUser.getCreatedAt())
            .message("User registered successfully")
            .build();
  }

  @Override
  @Transactional
  public PromoteUserResponse promoteToAdmin(PromoteUserRequest request) {
    log.debug("Attempting to promote user ID {} to ADMIN", request.getUserId());

    // Find user
    AdminUser user = adminUserRepository.findById(request.getUserId())
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getUserId()));

    // Check if already admin
    if (user.getRole() == Role.ADMIN) {
      throw new InvalidRequestException("User is already an ADMIN");
    }

    String previousRole = user.getRole().name();
    user.setRole(Role.ADMIN);
    adminUserRepository.save(user);

    log.info("User {} promoted from {} to ADMIN", user.getUsername(), previousRole);

    return PromoteUserResponse.builder()
            .userId(user.getId())
            .username(user.getUsername())
            .previousRole(previousRole)
            .newRole(Role.ADMIN.name())
            .message("User promoted to ADMIN successfully")
            .build();
  }
}
