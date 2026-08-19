package com.newspaper.library.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.newspaper.library.dto.common.GenericResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Custom access denied handler for 403 Forbidden errors.
 * <p>
 * Handles cases where user is authenticated but lacks required permissions.
 * Example: Regular user trying to access ADMIN-only endpoint.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

  private final ObjectMapper objectMapper;

  @Override
  public void handle(HttpServletRequest request,
                     HttpServletResponse response,
                     AccessDeniedException accessDeniedException) throws IOException, ServletException {

    log.warn("Access denied for {} {}: {}",
            request.getMethod(),
            request.getRequestURI(),
            accessDeniedException.getMessage());

    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);

    String message = "Access denied. You do not have permission to access this resource. " +
            "Required role: ADMIN";

    GenericResponse<Object> errorResponse = GenericResponse.error(403, message);
    response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
  }
}
