package com.manolito.dashflow.service;

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

    private final int TEST_PROJECT_ID = 123;
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

        Map<String, Integer> result = issuesService.getIssueCountsByProjectSeverityAndPriority(
                TEST_PROJECT_ID, TEST_SEVERITY, TEST_PRIORITY);

        assertEquals(3, result.size());
        assertEquals(5, result.get("Bug"));
        assertEquals(3, result.get("Enhancement"));
        assertEquals(1, result.get("Question"));

        verify(issuesDataWarehouseRepository, times(3)).getIssueCountByType(anyInt(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("getIssueCountsByProjectSeverityAndPriority - should throw when no issues found")
    void getIssueCountsByProjectSeverityAndPriority_whenNoIssues_shouldThrow() {
        when(issuesDataWarehouseRepository.getIssueCountByType(TEST_PROJECT_ID, TEST_SEVERITY.getValue(), TEST_PRIORITY.getValue(), "Bug"))
                .thenReturn(Optional.of(0));
        when(issuesDataWarehouseRepository.getIssueCountByType(TEST_PROJECT_ID, TEST_SEVERITY.getValue(), TEST_PRIORITY.getValue(), "Enhancement"))
                .thenReturn(Optional.of(0));
        when(issuesDataWarehouseRepository.getIssueCountByType(TEST_PROJECT_ID, TEST_SEVERITY.getValue(), TEST_PRIORITY.getValue(), "Question"))
                .thenReturn(Optional.of(0));

        assertThrows(NoSuchElementException.class,
                () -> issuesService.getIssueCountsByProjectSeverityAndPriority(
                        TEST_PROJECT_ID, TEST_SEVERITY, TEST_PRIORITY));

        verify(issuesDataWarehouseRepository, times(3)).getIssueCountByType(anyInt(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("getAllCurrentIssuesGroupedByType - should return grouped issues when they exist")
    void getAllCurrentIssuesGroupedByType_whenIssuesExist_shouldReturnGroupedMap() {
        Map<String, Integer> expected = new HashMap<>();
        expected.put("Bug", 5);
        expected.put("Enhancement", 3);

        when(issuesDataWarehouseRepository.getAllCurrentIssuesGroupedByType(TEST_PROJECT_ID))
                .thenReturn(expected);

        Map<String, Integer> result = issuesService.getAllCurrentIssuesGroupedByType(TEST_PROJECT_ID);

        assertEquals(expected, result);
        verify(issuesDataWarehouseRepository).getAllCurrentIssuesGroupedByType(TEST_PROJECT_ID);
    }

    @Test
    @DisplayName("getAllCurrentIssuesGroupedByType - should throw when no issues found")
    void getAllCurrentIssuesGroupedByType_whenNoIssues_shouldThrow() {
        when(issuesDataWarehouseRepository.getAllCurrentIssuesGroupedByType(TEST_PROJECT_ID))
                .thenReturn(Collections.emptyMap());

        assertThrows(NoSuchElementException.class,
                () -> issuesService.getAllCurrentIssuesGroupedByType(TEST_PROJECT_ID));

        verify(issuesDataWarehouseRepository).getAllCurrentIssuesGroupedByType(TEST_PROJECT_ID);
    }
}