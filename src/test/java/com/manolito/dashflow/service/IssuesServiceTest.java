package com.manolito.dashflow.service;

import com.manolito.dashflow.dto.dw.IssueCountDto;
import com.manolito.dashflow.dto.dw.IssueFilterRequestDto;
import com.manolito.dashflow.enums.IssuePriority;
import com.manolito.dashflow.enums.IssueSeverity;
import com.manolito.dashflow.repository.application.IssuesDataWarehouseRepository;
import com.manolito.dashflow.service.application.IssuesService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class IssuesServiceTest {

    @Mock
    private IssuesDataWarehouseRepository issuesDataWarehouseRepository;

    @InjectMocks
    private IssuesService issuesService;

    private final String TEST_PROJECT_ID = "123";
    private final IssueSeverity TEST_SEVERITY = IssueSeverity.IMPORTANT;
    private final IssuePriority TEST_PRIORITY = IssuePriority.HIGH;

    @Test
    @DisplayName("getIssueCountsByFilter - should return counts when issues exist with filters")
    void getIssueCountsByFilter_whenIssuesExistWithFilters_shouldReturnCounts() {
        IssueFilterRequestDto filter = IssueFilterRequestDto.builder()
                .projectId(TEST_PROJECT_ID)
                .severities(List.of(TEST_SEVERITY))
                .priorities(List.of(TEST_PRIORITY))
                .build();

        List<IssueCountDto> expected = List.of(
                new IssueCountDto("Bug", 5),
                new IssueCountDto("Enhancement", 3),
                new IssueCountDto("Question", 1)
        );

        when(issuesDataWarehouseRepository.getIssueCountsByFilter(filter))
                .thenReturn(expected);

        List<IssueCountDto> result = issuesService.getIssueCountsByFilter(filter);

        assertEquals(3, result.size());
        assertEquals(expected, result);
        verify(issuesDataWarehouseRepository).getIssueCountsByFilter(filter);
    }

    @Test
    @DisplayName("getIssueCountsByFilter - should return counts when issues exist without filters")
    void getIssueCountsByFilter_whenIssuesExistWithoutFilters_shouldReturnCounts() {
        IssueFilterRequestDto filter = IssueFilterRequestDto.builder()
                .projectId(TEST_PROJECT_ID)
                .build();

        List<IssueCountDto> expected = List.of(
                new IssueCountDto("Bug", 2),
                new IssueCountDto("Question", 1)
        );

        when(issuesDataWarehouseRepository.getIssueCountsByFilter(filter))
                .thenReturn(expected);

        List<IssueCountDto> result = issuesService.getIssueCountsByFilter(filter);

        assertEquals(2, result.size());
        assertEquals(expected, result);
        verify(issuesDataWarehouseRepository).getIssueCountsByFilter(filter);
    }

    @Test
    @DisplayName("getIssueCountsByFilter - should throw when filter is null")
    void getIssueCountsByFilter_whenFilterIsNull_shouldThrow() {
        assertThrows(IllegalArgumentException.class,
                () -> issuesService.getIssueCountsByFilter(null));

        verifyNoInteractions(issuesDataWarehouseRepository);
    }

    @Test
    @DisplayName("getIssueCountsByFilter - should throw when projectId is null")
    void getIssueCountsByFilter_whenProjectIdIsNull_shouldThrow() {
        IssueFilterRequestDto filter = IssueFilterRequestDto.builder()
                .projectId(null)
                .build();

        assertThrows(IllegalArgumentException.class,
                () -> issuesService.getIssueCountsByFilter(filter));

        verifyNoInteractions(issuesDataWarehouseRepository);
    }

    @Test
    @DisplayName("getAllCurrentIssuesGroupedByType - should return grouped issues when they exist")
    void getAllCurrentIssuesGroupedByType_whenIssuesExist_shouldReturnGroupedList() {
        List<IssueCountDto> expected = List.of(
                new IssueCountDto("Bug", 5),
                new IssueCountDto("Enhancement", 3)
        );

        when(issuesDataWarehouseRepository.getAllCurrentIssuesGroupedByType(TEST_PROJECT_ID))
                .thenReturn(expected);

        List<IssueCountDto> result = issuesService.getAllCurrentIssuesGroupedByType(TEST_PROJECT_ID);

        assertEquals(expected.size(), result.size());
        assertEquals(expected, result);
        verify(issuesDataWarehouseRepository).getAllCurrentIssuesGroupedByType(TEST_PROJECT_ID);
    }

    @Test
    @DisplayName("getAllCurrentIssuesGroupedByType - should throw when no issues found")
    void getAllCurrentIssuesGroupedByType_whenNoIssues_shouldThrow() {
        when(issuesDataWarehouseRepository.getAllCurrentIssuesGroupedByType(TEST_PROJECT_ID))
                .thenReturn(Collections.emptyList());

        assertThrows(NoSuchElementException.class,
                () -> issuesService.getAllCurrentIssuesGroupedByType(TEST_PROJECT_ID));

        verify(issuesDataWarehouseRepository).getAllCurrentIssuesGroupedByType(TEST_PROJECT_ID);
    }
}
