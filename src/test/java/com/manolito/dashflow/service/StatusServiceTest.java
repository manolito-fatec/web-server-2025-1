package com.manolito.dashflow.service;

import com.manolito.dashflow.dto.dw.StatusCountDto;
import com.manolito.dashflow.repository.application.TasksDataWarehouseRepository;
import com.manolito.dashflow.service.application.StatusService;
import org.junit.jupiter.api.BeforeEach;
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
    private final int TEST_PROJECT_ID = 456;

    @Test
    void getStatusCountGroupByStatusByUserIdAndProjectId_shouldReturnStatusCounts() {
        // Arrange
        List<StatusCountDto> expectedResults = List.of(
                new StatusCountDto("To Do", 5),
                new StatusCountDto("In Progress", 3),
                new StatusCountDto("Done", 2)
        );

        when(tasksDataWarehouseRepository.getStatusCountGroupByStatusByUserIdAndProjectId(
                TEST_USER_ID, TEST_PROJECT_ID))
                .thenReturn(expectedResults);

        // Act
        List<StatusCountDto> actualResults = statusService.getStatusCountGroupByStatusByUserIdAndProjectId(
                TEST_USER_ID, TEST_PROJECT_ID);

        // Assert
        assertNotNull(actualResults);
        assertEquals(3, actualResults.size());
        assertEquals("To Do", actualResults.get(0).getStatusName());
        assertEquals(5, actualResults.get(0).getCount());
        verify(tasksDataWarehouseRepository, times(1))
                .getStatusCountGroupByStatusByUserIdAndProjectId(TEST_USER_ID, TEST_PROJECT_ID);
    }

    @Test
    void getStatusCountGroupByStatusByUserIdAndProjectId_whenNoResults_shouldThrowException() {
        when(tasksDataWarehouseRepository.getStatusCountGroupByStatusByUserIdAndProjectId(
                anyInt(), anyInt()))
                .thenReturn(Collections.emptyList());

        assertThrows(NoSuchElementException.class, () ->
                statusService.getStatusCountGroupByStatusByUserIdAndProjectId(TEST_USER_ID, TEST_PROJECT_ID)
        );

        verify(tasksDataWarehouseRepository, times(1))
                .getStatusCountGroupByStatusByUserIdAndProjectId(TEST_USER_ID, TEST_PROJECT_ID);
    }

    @Test
    void getStatusCountGroupByStatusByUserIdAndProjectId_withNullInput_shouldThrowException() {
        assertThrows(NullPointerException.class, () ->
                statusService.getStatusCountGroupByStatusByUserIdAndProjectId(null, TEST_PROJECT_ID)
        );

        assertThrows(NullPointerException.class, () ->
                statusService.getStatusCountGroupByStatusByUserIdAndProjectId(TEST_USER_ID, null)
        );

        verify(tasksDataWarehouseRepository, never())
                .getStatusCountGroupByStatusByUserIdAndProjectId(anyInt(), anyInt());
    }
}