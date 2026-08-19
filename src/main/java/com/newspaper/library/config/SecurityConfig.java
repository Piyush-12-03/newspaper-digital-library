package com.newspaper.library.config;

import com.newspaper.library.security.JwtAccessDeniedHandler;
import com.newspaper.library.security.JwtAuthenticationEntryPoint;
import com.newspaper.library.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security configuration for the application.
 * <p>
 * Strategy: Allow ALL requests by default, then use @PreAuthorize on specific endpoints
 * to require ADMIN authentication. This ensures public endpoints work without issues.
 * <p>
 * Protected endpoints (via @PreAuthorize annotation):
 * - POST /api/v1/issues - Upload PDF (requires ADMIN role)
 * - POST /api/v1/auth/promote - Promote user to ADMIN (requires ADMIN role)
 * <p>
 * All other endpoints are PUBLIC (no authentication required):
 * - POST /api/v1/auth/login
 * - POST /api/v1/auth/register
 * - GET /api/v1/editions/**
 * - GET /api/v1/issues/**
 * <p>
 * Authentication Responses:
 * - 401 Unauthorized: Missing, invalid, or expired token
 * - 403 Forbidden: Valid token but insufficient permissions
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity  // Required for @PreAuthorize to work
public class SecurityConfig {

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                  JwtAuthenticationFilter jwtAuthenticationFilter,
                                                  JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
                                                  JwtAccessDeniedHandler jwtAccessDeniedHandler) {
    http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                    // Allow ALL requests - security is handled by @PreAuthorize on controllers
                    .anyRequest().permitAll()
            )
            .sessionManagement(session -> session
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            // Configure custom authentication error handlers
            .exceptionHandling(exceptions -> exceptions
                    .authenticationEntryPoint(jwtAuthenticationEntryPoint)  // 401 Unauthorized
                    .accessDeniedHandler(jwtAccessDeniedHandler)            // 403 Forbidden
            )
            // Add JWT filter to parse tokens (for @PreAuthorize to work)
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
