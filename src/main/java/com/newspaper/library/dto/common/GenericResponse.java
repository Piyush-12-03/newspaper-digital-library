package com.newspaper.library.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * Generic response wrapper for ALL API responses (success and error).
 * Provides consistent structure across the entire application.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Generic API response structure")
public class GenericResponse<T> {

  @Schema(description = "HTTP status code", example = "200")
  private Integer statusCode;

  @Schema(description = "Response status", example = "success", allowableValues = {"success", "error"})
  private String status;

  @Schema(description = "Response message", example = "Operation completed successfully")
  private String message;

  @Schema(description = "Response data (null for errors)")
  private T data;

  /**
   * Create a success response with data and message
   */
  public static <T> GenericResponse<T> success(T data, String message) {
    return GenericResponse.<T>builder()
            .statusCode(HttpStatus.OK.value())
            .status("success")
            .message(message)
            .data(data)
            .build();
  }

  /**
   * Create a success response with custom status code
   */
  public static <T> GenericResponse<T> success(Integer statusCode, T data, String message) {
    return GenericResponse.<T>builder()
            .statusCode(statusCode)
            .status("success")
            .message(message)
            .data(data)
            .build();
  }

  /**
   * Create an error response
   */
  public static <T> GenericResponse<T> error(Integer statusCode, String message) {
    return GenericResponse.<T>builder()
            .statusCode(statusCode)
            .status("error")
            .message(message)
            .data(null)
            .build();
  }

  /**
   * Create an error response with data (for validation errors)
   */
  public static <T> GenericResponse<T> error(Integer statusCode, String message, T data) {
    return GenericResponse.<T>builder()
            .statusCode(statusCode)
            .status("error")
            .message(message)
            .data(data)
            .build();
  }
}
