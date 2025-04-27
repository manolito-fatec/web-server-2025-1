package com.manolito.dashflow.service;

import com.manolito.dashflow.dto.dw.IssueCountDto;
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
    @DisplayName("getIssueCountsByProjectSeverityAndPriority - should return counts when issues exist")
    void getIssueCountsByProjectSeverityAndPriority_whenIssuesExist_shouldReturnCounts() {
        when(issuesDataWarehouseRepository.getIssueCountByType(TEST_PROJECT_ID, TEST_SEVERITY.getValue(), TEST_PRIORITY.getValue(), "Bug"))
                .thenReturn(Optional.of(5));
        when(issuesDataWarehouseRepository.getIssueCountByType(TEST_PROJECT_ID, TEST_SEVERITY.getValue(), TEST_PRIORITY.getValue(), "Enhancement"))
                .thenReturn(Optional.of(3));
        when(issuesDataWarehouseRepository.getIssueCountByType(TEST_PROJECT_ID, TEST_SEVERITY.getValue(), TEST_PRIORITY.getValue(), "Question"))
                .thenReturn(Optional.of(1));

        List<IssueCountDto> result = issuesService.getIssueCountsByProjectSeverityAndPriority(
                TEST_PROJECT_ID, TEST_SEVERITY, TEST_PRIORITY);

        assertEquals(3, result.size());
        assertTrue(result.stream().anyMatch(ic -> "Bug".equals(ic.getType()) && ic.getCount() == 5));
        assertTrue(result.stream().anyMatch(ic -> "Enhancement".equals(ic.getType()) && ic.getCount() == 3));
        assertTrue(result.stream().anyMatch(ic -> "Question".equals(ic.getType()) && ic.getCount() == 1));

        verify(issuesDataWarehouseRepository, times(3)).getIssueCountByType(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("getIssueCountsByProjectSeverityAndPriority - should return empty list when no issues found")
    void getIssueCountsByProjectSeverityAndPriority_whenNoIssues_shouldReturnEmptyList() {
        when(issuesDataWarehouseRepository.getIssueCountByType(TEST_PROJECT_ID, TEST_SEVERITY.getValue(), TEST_PRIORITY.getValue(), "Bug"))
                .thenReturn(Optional.of(0));
        when(issuesDataWarehouseRepository.getIssueCountByType(TEST_PROJECT_ID, TEST_SEVERITY.getValue(), TEST_PRIORITY.getValue(), "Enhancement"))
                .thenReturn(Optional.of(0));
        when(issuesDataWarehouseRepository.getIssueCountByType(TEST_PROJECT_ID, TEST_SEVERITY.getValue(), TEST_PRIORITY.getValue(), "Question"))
                .thenReturn(Optional.of(0));

        List<IssueCountDto> result = issuesService.getIssueCountsByProjectSeverityAndPriority(
                TEST_PROJECT_ID, TEST_SEVERITY, TEST_PRIORITY);

        assertEquals(3, result.size());
        assertEquals(0, result.get(0).getCount());
        assertEquals(0, result.get(1).getCount());
        assertEquals(0, result.get(2).getCount());

        verify(issuesDataWarehouseRepository, times(3)).getIssueCountByType(anyString(), anyString(), anyString(), anyString());
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
        assertTrue(result.containsAll(expected));
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
