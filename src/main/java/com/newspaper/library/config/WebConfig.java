package com.newspaper.library.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.UUID;

/**
 * Web MVC configuration.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

  @Override
  public void addInterceptors(@NonNull InterceptorRegistry registry) {
    registry.addInterceptor(new RequestTraceInterceptor());
  }

  /**
   * Interceptor to handle request trace IDs.
   */
  public static class RequestTraceInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler) {

      String traceId = request.getHeader("X-Request-ID");
      if (traceId == null || traceId.isBlank()) {
        traceId = UUID.randomUUID().toString();
      }

      // Add to response header
      response.setHeader("X-Request-ID", traceId);

      return true;
    }
  }
}
