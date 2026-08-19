package com.newspaper.library.service;

import com.newspaper.library.config.NewspaperApiProperties;
import com.newspaper.library.dto.issue.IssueDetailResponse;
import com.newspaper.library.dto.issue.IssueSummaryResponse;
import com.newspaper.library.entity.Edition;
import com.newspaper.library.entity.Issue;
import com.newspaper.library.enums.ApiErrorCode;
import com.newspaper.library.enums.EditionType;
import com.newspaper.library.exception.InvalidDateRangeException;
import com.newspaper.library.exception.ResourceNotFoundException;
import com.newspaper.library.mapper.IssueMapper;
import com.newspaper.library.repository.IssueRepository;
import com.newspaper.library.service.impl.IssueServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("IssueService Tests")
class IssueServiceTest {
    
    @Mock
    private IssueRepository issueRepository;
    
    @Mock
    private IssueMapper issueMapper;
    
    @Mock
    private NewspaperApiProperties apiProperties;
    
    @InjectMocks
    private IssueServiceImpl issueService;
    
    private Edition edition;
    private Issue issue;
    private IssueSummaryResponse issueSummary;
    private IssueDetailResponse issueDetail;
    
    @BeforeEach
    void setUp() {
        // Setup test data
        edition = new Edition();
        edition.setId(1L);
        edition.setName("Bhopal City");
        edition.setSlug("bhopal-city");
        edition.setCity("Bhopal");
        edition.setRegion("Madhya Pradesh");
        edition.setEditionType(EditionType.CITY);
        edition.setActive(true);
        edition.setCreatedAt(Instant.now());
        edition.setUpdatedAt(Instant.now());
        
        issue = new Issue();
        issue.setId(1L);
        issue.setEdition(edition);
        issue.setPublicationDate(LocalDate.of(2026, 8, 15));
        issue.setFileName("bhopal-city-2026-08-15.pdf");
        issue.setContentType("application/pdf");
        issue.setFileSize(2048576L);
        issue.setPageCount(12);
        issue.setCreatedAt(Instant.now());
        issue.setUpdatedAt(Instant.now());
        
        issueSummary = new IssueSummaryResponse();
        issueDetail = new IssueDetailResponse();
    }
    
    @Test
    @DisplayName("Should get issue by ID successfully")
    void shouldGetIssueById() {
        when(issueRepository.findById(1L)).thenReturn(Optional.of(issue));
        when(issueMapper.toDetail(issue)).thenReturn(issueDetail);
        
        IssueDetailResponse result = issueService.getIssueById(1L);
        
        assertThat(result).isNotNull();
        verify(issueRepository).findById(1L);
        verify(issueMapper).toDetail(issue);
    }
    
    @Test
    @DisplayName("Should throw exception when issue not found")
    void shouldThrowExceptionWhenIssueNotFound() {
        when(issueRepository.findById(999L)).thenReturn(Optional.empty());
        
        assertThatThrownBy(() -> issueService.getIssueById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Issue not found with issueId: 999");
    }
    
    @Test
    @DisplayName("Should get issues by specific date")
    void shouldGetIssuesByDate() {
        LocalDate date = LocalDate.of(2026, 8, 15);
        Pageable pageable = PageRequest.of(0, 20);
        Page<Issue> issuePage = new PageImpl<>(List.of(issue));
        
        when(issueRepository.findByPublicationDate(eq(date), eq(pageable)))
                .thenReturn(issuePage);
        when(issueMapper.toSummary(any())).thenReturn(issueSummary);
        
        Page<IssueSummaryResponse> result = issueService.getIssues(date, null, null, null, pageable);
        
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(issueRepository).findByPublicationDate(date, pageable);
    }
    
    @Test
    @DisplayName("Should get issues by date range")
    void shouldGetIssuesByDateRange() {
        when(apiProperties.getMaxDateRangeDays()).thenReturn(90);
        LocalDate from = LocalDate.of(2026, 8, 1);
        LocalDate to = LocalDate.of(2026, 8, 15);
        Pageable pageable = PageRequest.of(0, 20);
        Page<Issue> issuePage = new PageImpl<>(List.of(issue));
        
        when(issueRepository.findByPublicationDateBetween(eq(from), eq(to), eq(pageable)))
                .thenReturn(issuePage);
        when(issueMapper.toSummary(any())).thenReturn(issueSummary);
        
        Page<IssueSummaryResponse> result = issueService.getIssues(null, from, to, null, pageable);
        
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(issueRepository).findByPublicationDateBetween(from, to, pageable);
    }
    
    @Test
    @DisplayName("Should get issues by date and edition")
    void shouldGetIssuesByDateAndEdition() {
        LocalDate date = LocalDate.of(2026, 8, 15);
        Long editionId = 1L;
        Pageable pageable = PageRequest.of(0, 20);
        Page<Issue> issuePage = new PageImpl<>(List.of(issue));
        
        when(issueRepository.findByPublicationDateAndEditionId(eq(date), eq(editionId), eq(pageable)))
                .thenReturn(issuePage);
        when(issueMapper.toSummary(any())).thenReturn(issueSummary);
        
        Page<IssueSummaryResponse> result = issueService.getIssues(date, null, null, editionId, pageable);
        
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(issueRepository).findByPublicationDateAndEditionId(date, editionId, pageable);
    }
    
    @Test
    @DisplayName("Should throw exception when both date and range are specified")
    void shouldThrowExceptionForConflictingFilters() {
        LocalDate date = LocalDate.of(2026, 8, 15);
        LocalDate from = LocalDate.of(2026, 8, 1);
        Pageable pageable = PageRequest.of(0, 20);
        
        assertThatThrownBy(() -> issueService.getIssues(date, from, null, null, pageable))
                .isInstanceOf(InvalidDateRangeException.class)
                .hasMessageContaining("Cannot specify both 'date' and date range");
    }
    
    @Test
    @DisplayName("Should throw exception when from is after to")
    void shouldThrowExceptionWhenFromAfterTo() {
        LocalDate from = LocalDate.of(2026, 8, 15);
        LocalDate to = LocalDate.of(2026, 8, 1);
        Pageable pageable = PageRequest.of(0, 20);
        
        assertThatThrownBy(() -> issueService.getIssues(null, from, to, null, pageable))
                .isInstanceOf(InvalidDateRangeException.class)
                .hasMessageContaining("'from' date cannot be after the 'to' date");
    }
    
    @Test
    @DisplayName("Should throw exception when date range exceeds maximum")
    void shouldThrowExceptionWhenDateRangeTooLarge() {
        when(apiProperties.getMaxDateRangeDays()).thenReturn(90);
        LocalDate from = LocalDate.of(2026, 1, 1);
        LocalDate to = LocalDate.of(2026, 12, 31);
        Pageable pageable = PageRequest.of(0, 20);
        
        assertThatThrownBy(() -> issueService.getIssues(null, from, to, null, pageable))
                .isInstanceOf(InvalidDateRangeException.class)
                .hasMessageContaining("Date range cannot exceed 90 days");
    }
    
    @Test
    @DisplayName("Should throw exception when only 'from' is provided")
    void shouldThrowExceptionWhenOnlyFromProvided() {
        LocalDate from = LocalDate.of(2026, 8, 1);
        Pageable pageable = PageRequest.of(0, 20);
        
        assertThatThrownBy(() -> issueService.getIssues(null, from, null, null, pageable))
                .isInstanceOf(InvalidDateRangeException.class)
                .hasMessageContaining("Both 'from' and 'to' dates must be specified");
    }
    
    @Test
    @DisplayName("Should get all issues when no filters provided")
    void shouldGetAllIssuesWithNoFilters() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Issue> issuePage = new PageImpl<>(List.of(issue));
        
        when(issueRepository.findAll(eq(pageable))).thenReturn(issuePage);
        when(issueMapper.toSummary(any())).thenReturn(issueSummary);
        
        Page<IssueSummaryResponse> result = issueService.getIssues(null, null, null, null, pageable);
        
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(issueRepository).findAll(pageable);
    }
    
    @Test
    @DisplayName("Should get issues by edition only")
    void shouldGetIssuesByEditionOnly() {
        Long editionId = 1L;
        Pageable pageable = PageRequest.of(0, 20);
        Page<Issue> issuePage = new PageImpl<>(List.of(issue));
        
        when(issueRepository.findByEditionId(eq(editionId), eq(pageable)))
                .thenReturn(issuePage);
        when(issueMapper.toSummary(any())).thenReturn(issueSummary);
        
        Page<IssueSummaryResponse> result = issueService.getIssues(null, null, null, editionId, pageable);
        
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(issueRepository).findByEditionId(editionId, pageable);
    }
    
    @Test
    @DisplayName("Should return empty page when no issues found")
    void shouldReturnEmptyPageWhenNoIssuesFound() {
        LocalDate date = LocalDate.of(2025, 1, 1);
        Pageable pageable = PageRequest.of(0, 20);
        Page<Issue> emptyPage = new PageImpl<>(List.of());
        
        when(issueRepository.findByPublicationDate(eq(date), eq(pageable)))
                .thenReturn(emptyPage);
        
        Page<IssueSummaryResponse> result = issueService.getIssues(date, null, null, null, pageable);
        
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }
}
