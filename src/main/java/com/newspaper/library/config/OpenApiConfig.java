package com.newspaper.library.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI/Swagger configuration for API documentation.
 * Security requirements are applied per-endpoint using @SecurityRequirement annotation,
 * not globally, to allow public endpoints like login.
 */
@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI newspaperLibraryOpenAPI() {
    return new OpenAPI()
            .info(new Info()
                    .title("Newspaper Digital Library API")
                    .description("Backend API for managing newspaper editions and daily PDF issues. " +
                            "Public endpoints: login, list editions, list issues, download PDF. " +
                            "Protected endpoints: upload PDF (requires ADMIN role).")
                    .version("1.0.0")
                    .contact(new Contact()
                            .name("Newspaper Digital Library Team")
                            .email("support@newspaper-library.com"))
                    .license(new License()
                            .name("Apache 2.0")
                            .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
            // REMOVED global security requirement - each endpoint specifies its own security
            .components(new Components()
                    .addSecuritySchemes("Bearer Authentication",
                            new SecurityScheme()
                                    .type(SecurityScheme.Type.HTTP)
                                    .scheme("bearer")
                                    .bearerFormat("JWT")
                                    .description("Enter JWT token obtained from /auth/login endpoint")));
  }
}
