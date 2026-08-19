package com.newspaper.library.exception;

import com.newspaper.library.enums.ApiErrorCode;
import lombok.Getter;

/**
 * Exception thrown when date range validation fails.
 */
@Getter
public class InvalidDateRangeException extends RuntimeException {

  private final ApiErrorCode errorCode;

  public InvalidDateRangeException(String message, ApiErrorCode errorCode) {
    super(message);
    this.errorCode = errorCode;
  }
}
