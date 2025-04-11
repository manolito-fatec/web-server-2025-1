package com.manolito.dashflow.service;

import com.manolito.dashflow.dto.dw.StatusCountDto;
import com.manolito.dashflow.repository.application.TasksDataWarehouseRepository;
import com.manolito.dashflow.service.application.StatusService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatusServiceTest {

    @Mock
    private TasksDataWarehouseRepository tasksDataWarehouseRepository;

    @InjectMocks
    private StatusService statusService;

    private final int TEST_USER_ID = 123;
    private final String TEST_PROJECT_ID = "456";

    @Test
    @DisplayName("Should return task counts grouped by status when valid user and project IDs are provided")
    void getStatusCountGroupByTaskByUserIdAndProjectId_shouldReturnStatusCounts() {
        List<StatusCountDto> expectedResults = List.of(
                new StatusCountDto("To Do", 5),
                new StatusCountDto("In Progress", 3),
                new StatusCountDto("Done", 2)
        );

        when(tasksDataWarehouseRepository.getTaskCountGroupByStatusByUserIdAndProjectId(
                TEST_USER_ID, TEST_PROJECT_ID))
                .thenReturn(expectedResults);

        List<StatusCountDto> actualResults = statusService.getTaskCountGroupByStatusByUserIdAndProjectId(
                TEST_USER_ID, TEST_PROJECT_ID);

        assertNotNull(actualResults);
        assertEquals(3, actualResults.size());
        assertEquals("To Do", actualResults.get(0).getStatusName());
        assertEquals(5, actualResults.get(0).getCount());
        verify(tasksDataWarehouseRepository, times(1))
                .getTaskCountGroupByStatusByUserIdAndProjectId(TEST_USER_ID, TEST_PROJECT_ID);
    }

    @Test
    @DisplayName("Should throw NoSuchElementException when no task status counts are found")
    void getTaskCountGroupByStatusByUserIdAndProjectId_whenNoResults_shouldThrowException() {
        when(tasksDataWarehouseRepository.getTaskCountGroupByStatusByUserIdAndProjectId(
                anyInt(), anyString()))
                .thenReturn(Collections.emptyList());

        assertThrows(NoSuchElementException.class, () ->
                statusService.getTaskCountGroupByStatusByUserIdAndProjectId(TEST_USER_ID, TEST_PROJECT_ID)
        );

        verify(tasksDataWarehouseRepository, times(1))
                .getTaskCountGroupByStatusByUserIdAndProjectId(TEST_USER_ID, TEST_PROJECT_ID);
    }

    @Test
    @DisplayName("Should throw NullPointerException when either user ID or project ID is null")
    void getTaskCountGroupByStatusByUserIdAndProjectId_withNullInput_shouldThrowException() {
        assertThrows(NullPointerException.class, () ->
                statusService.getTaskCountGroupByStatusByUserIdAndProjectId(null, TEST_PROJECT_ID)
        );

        assertThrows(NullPointerException.class, () ->
                statusService.getTaskCountGroupByStatusByUserIdAndProjectId(TEST_USER_ID, null)
        );

        verify(tasksDataWarehouseRepository, never())
                .getTaskCountGroupByStatusByUserIdAndProjectId(anyInt(), anyString());
    }

    @Test
    @DisplayName("Should return task counts grouped by status when valid project ID is provided")
    void getTaskCountGroupByStatusByProjectId_shouldReturnStatusCounts() {
        List<StatusCountDto> expectedResults = List.of(
                new StatusCountDto("To Do", 10),
                new StatusCountDto("In Progress", 6),
                new StatusCountDto("Done", 4)
        );

        when(tasksDataWarehouseRepository.getTaskCountGroupByStatusByProjectId(TEST_PROJECT_ID))
                .thenReturn(expectedResults);

        List<StatusCountDto> actualResults = statusService.getTaskCountGroupByStatusByProjectId(TEST_PROJECT_ID);

        assertNotNull(actualResults);
        assertEquals(3, actualResults.size());
        assertEquals("To Do", actualResults.get(0).getStatusName());
        assertEquals(10, actualResults.get(0).getCount());
        verify(tasksDataWarehouseRepository, times(1))
                .getTaskCountGroupByStatusByProjectId(TEST_PROJECT_ID);
    }

    @Test
    @DisplayName("Should throw NoSuchElementException when no task status counts are found for project")
    void getTaskCountGroupByStatusByProjectId_whenNoResults_shouldThrowException() {
        when(tasksDataWarehouseRepository.getTaskCountGroupByStatusByProjectId(anyString()))
                .thenReturn(Collections.emptyList());

        assertThrows(NoSuchElementException.class, () ->
                statusService.getTaskCountGroupByStatusByProjectId(TEST_PROJECT_ID)
        );

        verify(tasksDataWarehouseRepository, times(1))
                .getTaskCountGroupByStatusByProjectId(TEST_PROJECT_ID);
    }

    @Test
    @DisplayName("Should throw NullPointerException when project ID is null")
    void getTaskCountGroupByStatusByProjectId_withNullInput_shouldThrowException() {
        assertThrows(NullPointerException.class, () ->
                statusService.getTaskCountGroupByStatusByProjectId(null)
        );

        verify(tasksDataWarehouseRepository, never())
                .getTaskCountGroupByStatusByProjectId(anyString());
    }
}