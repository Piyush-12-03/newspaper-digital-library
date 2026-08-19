package com.newspaper.library.mapper;

import com.newspaper.library.dto.edition.EditionResponse;
import com.newspaper.library.dto.edition.EditionSummaryResponse;
import com.newspaper.library.entity.Edition;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between Edition entity and DTOs.
 */
@Component
public class EditionMapper {

  public EditionResponse toResponse(Edition edition) {
    if (edition == null) {
      return null;
    }

    return new EditionResponse(
            edition.getId(),
            edition.getName(),
            edition.getSlug(),
            edition.getCity(),
            edition.getRegion(),
            edition.getEditionType(),
            edition.getActive(),
            edition.getCreatedAt(),
            edition.getUpdatedAt()
    );
  }

  public EditionSummaryResponse toSummary(Edition edition) {
    if (edition == null) {
      return null;
    }

    return new EditionSummaryResponse(
            edition.getId(),
            edition.getName(),
            edition.getSlug(),
            edition.getCity(),
            edition.getEditionType()
    );
  }
}
