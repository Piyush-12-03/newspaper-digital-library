package com.newspaper.library.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.newspaper.library.dto.common.GenericResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Custom authentication entry point to return proper JSON responses
 * instead of default Spring Security HTML error pages.
 * <p>
 * Handles 401 Unauthorized errors when:
 * - Token is missing
 * - Token is invalid
 * - Token is expired
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

  private final ObjectMapper objectMapper;

  @Override
  public void commence(HttpServletRequest request,
                       HttpServletResponse response,
                       AuthenticationException authException) throws IOException, ServletException {

    log.warn("Authentication failed for {} {}: {}",
            request.getMethod(),
            request.getRequestURI(),
            authException.getMessage());

    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);

    // Check if token was missing or invalid
    String authHeader = request.getHeader("Authorization");
    String message;

    if (authHeader == null || authHeader.isBlank()) {
      message = "Authentication required. Please provide a valid JWT token in the Authorization header.";
    } else if (!authHeader.startsWith("Bearer ")) {
      message = "Invalid Authorization header format. Expected: Bearer <token>";
    } else {
      // Token was provided but invalid/expired
      String errorMsg = (String) request.getAttribute("jwt_error");
      if (errorMsg != null) {
        message = errorMsg;
      } else {
        message = "Invalid or expired authentication token";
      }
    }

    GenericResponse<Object> errorResponse = GenericResponse.error(401, message);
    response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
  }
}
