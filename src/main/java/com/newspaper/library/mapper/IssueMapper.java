package com.newspaper.library.mapper;

import com.newspaper.library.dto.edition.IssueResponse;
import com.newspaper.library.dto.issue.IssueDetailResponse;
import com.newspaper.library.dto.issue.IssueSummaryResponse;
import com.newspaper.library.entity.Issue;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between Issue entity and DTOs.
 */
@Component
@RequiredArgsConstructor
public class IssueMapper {

  private final EditionMapper editionMapper;

  public IssueResponse toResponse(Issue issue) {
    if (issue == null) {
      return null;
    }

    return new IssueResponse(
            issue.getId(),
            issue.getEdition().getId(),
            issue.getEdition().getName(),
            issue.getPublicationDate(),
            issue.getFileName(),
            issue.getContentType(),
            issue.getFileSize(),
            issue.getPageCount(),
            issue.getCreatedAt(),
            issue.getUpdatedAt()
    );
  }

  public IssueSummaryResponse toSummary(Issue issue) {
    if (issue == null) {
      return null;
    }

    return new IssueSummaryResponse(
            issue.getId(),
            editionMapper.toSummary(issue.getEdition()),
            issue.getPublicationDate(),
            issue.getFileName(),
            issue.getFileSize(),
            issue.getPageCount()
    );
  }

  public IssueDetailResponse toDetail(Issue issue) {
    if (issue == null) {
      return null;
    }

    return new IssueDetailResponse(
            issue.getId(),
            editionMapper.toSummary(issue.getEdition()),
            issue.getPublicationDate(),
            issue.getFileName(),
            issue.getContentType(),
            issue.getFileSize(),
            issue.getPageCount(),
            issue.getCreatedAt(),
            issue.getUpdatedAt()
    );
  }
}
