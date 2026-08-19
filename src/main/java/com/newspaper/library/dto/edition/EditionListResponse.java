package com.newspaper.library.dto.edition;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for list of editions with their issues.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response containing a list of editions")
public class EditionListResponse {

  @Schema(description = "List of editions")
  private List<EditionResponse> editions;

  @Schema(description = "Total count of editions", example = "6")
  private int totalCount;
}
