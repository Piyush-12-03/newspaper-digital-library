package com.newspaper.library.security;

import com.newspaper.library.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT authentication filter to intercept and validate JWT tokens.
 * Extracts JWT from Authorization header, validates it, and sets authentication in SecurityContext.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtUtil jwtUtil;
  private final CustomUserDetailsService userDetailsService;

  @Override
  protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain filterChain) throws ServletException, IOException {
    try {
      String jwt = extractJwtFromRequest(request);

      if (jwt != null && SecurityContextHolder.getContext().getAuthentication() == null) {
        try {
          String username = jwtUtil.extractUsername(jwt);

          if (username != null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if (jwtUtil.validateToken(jwt, userDetails)) {
              UsernamePasswordAuthenticationToken authentication =
                      new UsernamePasswordAuthenticationToken(
                              userDetails,
                              null,
                              userDetails.getAuthorities()
                      );
              authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

              SecurityContextHolder.getContext().setAuthentication(authentication);
              log.debug("JWT authentication successful for user: {}", username);
            } else {
              request.setAttribute("jwt_error", "Invalid authentication token");
              log.warn("JWT validation failed for token");
            }
          }
        } catch (io.jsonwebtoken.ExpiredJwtException ex) {
          request.setAttribute("jwt_error", "Authentication token has expired");
          log.warn("Expired JWT token: {}", ex.getMessage());
        } catch (io.jsonwebtoken.MalformedJwtException ex) {
          request.setAttribute("jwt_error", "Malformed authentication token");
          log.warn("Malformed JWT token: {}", ex.getMessage());
        } catch (io.jsonwebtoken.SignatureException ex) {
          request.setAttribute("jwt_error", "Invalid token signature");
          log.warn("Invalid JWT signature: {}", ex.getMessage());
        } catch (io.jsonwebtoken.UnsupportedJwtException ex) {
          request.setAttribute("jwt_error", "Unsupported authentication token");
          log.warn("Unsupported JWT token: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
          request.setAttribute("jwt_error", "Invalid authentication token");
          log.warn("JWT token error: {}", ex.getMessage());
        }
      }
    } catch (Exception ex) {
      log.error("JWT authentication processing error: {}", ex.getMessage());
      request.setAttribute("jwt_error", "Authentication processing failed");
    }

    filterChain.doFilter(request, response);
  }

  /**
   * Extract JWT token from Authorization header.
   */
  private String extractJwtFromRequest(HttpServletRequest request) {
    String bearerToken = request.getHeader("Authorization");

    if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
      return bearerToken.substring(7);
    }

    return null;
  }
}
