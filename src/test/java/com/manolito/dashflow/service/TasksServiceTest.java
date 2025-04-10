package com.manolito.dashflow.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;

import com.manolito.dashflow.dto.dw.CreatedDoneDto;
import com.manolito.dashflow.repository.application.TasksDataWarehouseRepository;
import com.manolito.dashflow.service.application.TasksService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class TasksServiceTest {

    @Mock
    private TasksDataWarehouseRepository tasksDataWarehouseRepository;

    @InjectMocks
    private TasksService tasksService;

    private final int TEST_USER_ID = 123;
    private final int TEST_PROJECT_ID = 456;
    private final LocalDate TEST_START_DATE = LocalDate.parse("2024-01-01");
    private final LocalDate TEST_END_DATE = LocalDate.parse("2025-01-01");
    private final LocalDate INVALID_START_DATE = LocalDate.parse("7777-01-01"); // start date after end date
    private final LocalDate INVALID_END_DATE = LocalDate.parse("1111-01-01"); // end date before start date

    private final CreatedDoneDto TEST_PROJECT_STATS = new CreatedDoneDto(10, 5);
    private final List<CreatedDoneDto> TEST_STATUS_LIST = List.of(
            new CreatedDoneDto(8, 4),
            new CreatedDoneDto(5, 3)
    );

    @Test
    @DisplayName("getTaskCountByOperatorId - should return task count when tasks exist")
    void getTaskCountByOperatorId_whenTasksExist_shouldReturnCount() {
        when(tasksDataWarehouseRepository.getTotalTasksByOperator(TEST_USER_ID))
                .thenReturn(Optional.of(5));

        Integer result = tasksService.getTaskCountByOperatorId(TEST_USER_ID);

        assertEquals(5, result);
        verify(tasksDataWarehouseRepository).getTotalTasksByOperator(TEST_USER_ID);
    }

    @Test
    @DisplayName("getTaskCountByOperatorId - should throw when no tasks found")
    void getTaskCountByOperatorId_whenNoTasks_shouldThrow() {
        when(tasksDataWarehouseRepository.getTotalTasksByOperator(TEST_USER_ID))
                .thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> tasksService.getTaskCountByOperatorId(TEST_USER_ID));

        verify(tasksDataWarehouseRepository).getTotalTasksByOperator(TEST_USER_ID);
    }

    @Test
    @DisplayName("getTaskCountByOperatorId - should throw when userId is null")
    void getTaskCountByOperatorId_whenUserIdIsNull_shouldThrow() {
        assertThrows(NullPointerException.class,
                () -> tasksService.getTaskCountByOperatorId(null));
    }

    @Test
    @DisplayName("getTaskCountByOperatorIdBetween - should return count for valid period")
    void getTaskCountByOperatorIdBetween_whenValidPeriod_shouldReturnCount() {
        when(tasksDataWarehouseRepository.getTotalTasksByOperatorBetween(TEST_USER_ID, TEST_START_DATE, TEST_END_DATE))
                .thenReturn(Optional.of(8));

        Integer result = tasksService.getTaskCountByOperatorIdBetween(TEST_USER_ID, TEST_START_DATE, TEST_END_DATE);

        assertEquals(8, result);
        verify(tasksDataWarehouseRepository).getTotalTasksByOperatorBetween(TEST_USER_ID, TEST_START_DATE, TEST_END_DATE);
    }

    @Test
    @DisplayName("getTaskCountByOperatorIdBetween - should throw when no tasks in period")
    void getTaskCountByOperatorIdBetween_whenNoTasksInPeriod_shouldThrow() {
        when(tasksDataWarehouseRepository.getTotalTasksByOperatorBetween(TEST_USER_ID, TEST_START_DATE, TEST_END_DATE))
                .thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> tasksService.getTaskCountByOperatorIdBetween(TEST_USER_ID, TEST_START_DATE, TEST_END_DATE));
    }

    @Test
    @DisplayName("getTaskCountByOperatorIdBetween - should throw when dates are reversed")
    void getTaskCountByOperatorIdBetween_whenDatesReversed_shouldThrow() {
        assertThrows(IllegalArgumentException.class,
                () -> tasksService.getTaskCountByOperatorIdBetween(TEST_USER_ID, INVALID_START_DATE, INVALID_END_DATE));
    }

    @Test
    @DisplayName("getTaskCountByOperatorIdBetween - should throw when any date is null")
    void getTaskCountByOperatorIdBetween_whenDateIsNull_shouldThrow() {
        assertThrows(NullPointerException.class,
                () -> tasksService.getTaskCountByOperatorIdBetween(TEST_USER_ID, null, TEST_END_DATE));

        assertThrows(NullPointerException.class,
                () -> tasksService.getTaskCountByOperatorIdBetween(TEST_USER_ID, TEST_START_DATE, null));
    }

    @Test
    @DisplayName("getTaskCountByStatusByOperatorIdBetween - should throw when no tasks in period")
    void getTaskCountByStatusByOperatorIdBetween_whenNoTasks_shouldThrow() {
        when(tasksDataWarehouseRepository.getTotalTasksByStatusByOperatorBetween(TEST_USER_ID, TEST_START_DATE, TEST_END_DATE))
                .thenReturn(Collections.emptyList());

        assertThrows(NoSuchElementException.class,
                () -> tasksService.getTaskCountByStatusByOperatorIdBetween(TEST_USER_ID, TEST_START_DATE, TEST_END_DATE));
    }

    @Test
    @DisplayName("getTaskCountByStatusByOperatorIdBetween - should return stats when tasks exist")
    void getTaskCountByStatusByOperatorIdBetween_whenTasksExist_shouldReturnList() {
        when(tasksDataWarehouseRepository.getTotalTasksByStatusByOperatorBetween(
                TEST_USER_ID, TEST_START_DATE, TEST_END_DATE))
                .thenReturn(TEST_STATUS_LIST);

        List<CreatedDoneDto> result = tasksService.getTaskCountByStatusByOperatorIdBetween(
                TEST_USER_ID, TEST_START_DATE, TEST_END_DATE);

        assertEquals(2, result.size());
        assertEquals(8, result.get(0).getCreatedTaskCount());
        assertEquals(4, result.get(0).getCompletedTaskCount());
        assertEquals(5, result.get(1).getCreatedTaskCount());
        assertEquals(3, result.get(1).getCompletedTaskCount());
    }

    @Test
    @DisplayName("getTaskCountByStatusByOperatorIdBetween - should throw when dates are reversed")
    void getTaskCountByStatusByOperatorIdBetween_whenDatesReversed_shouldThrow() {
        assertThrows(IllegalArgumentException.class,
                () -> tasksService.getTaskCountByStatusByOperatorIdBetween(
                        TEST_PROJECT_ID, INVALID_START_DATE, INVALID_END_DATE));
    }

    @Test
    @DisplayName("getCreatedAndCompletedTaskCountByProjectBetween - should return DTO when tasks exist")
    void getCreatedAndCompletedTaskCountByProjectBetween_whenTasksExist_shouldReturnDto() {
        when(tasksDataWarehouseRepository.getAllCreatedAndCompletedTasksByProjectBetween(
                TEST_PROJECT_ID, TEST_START_DATE, TEST_END_DATE))
                .thenReturn(Optional.of(TEST_PROJECT_STATS));

        CreatedDoneDto result = tasksService.getCreatedAndCompletedTaskCountByProjectBetween(
                TEST_PROJECT_ID, TEST_START_DATE, TEST_END_DATE);

        assertEquals(10, result.getCreatedTaskCount());
        assertEquals(5, result.getCompletedTaskCount());
    }

    @Test
    @DisplayName("getCreatedAndCompletedTaskCountByProjectBetween - should throw when no tasks in period")
    void getCreatedAndCompletedTaskCountByProjectBetween_whenNoTasks_shouldThrow() {
        when(tasksDataWarehouseRepository.getAllCreatedAndCompletedTasksByProjectBetween(
                TEST_PROJECT_ID, TEST_START_DATE, TEST_END_DATE))
                .thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> tasksService.getCreatedAndCompletedTaskCountByProjectBetween(
                        TEST_PROJECT_ID, TEST_START_DATE, TEST_END_DATE));
    }

    @Test
    @DisplayName("getCreatedAndCompletedTaskCountByProjectBetween - should throw when projectId is null")
    void getCreatedAndCompletedTaskCountByProjectBetween_whenProjectIdIsNull_shouldThrow() {
        assertThrows(NullPointerException.class,
                () -> tasksService.getCreatedAndCompletedTaskCountByProjectBetween(
                        null, TEST_START_DATE, TEST_END_DATE));
    }

    @Test
    @DisplayName("getCreatedAndCompletedTaskCountByProjectBetween - should throw when dates are reversed")
    void getCreatedAndCompletedTaskCountByProjectBetween_whenDatesReversed_shouldThrow() {
        assertThrows(IllegalArgumentException.class,
                () -> tasksService.getCreatedAndCompletedTaskCountByProjectBetween(
                        TEST_PROJECT_ID, INVALID_START_DATE, INVALID_END_DATE));
    }

    /* getAverageTimeCard tests */
    @Test
    @DisplayName("getAverageTimeCard - should return average when tasks exist")
    void getAverageTimeCard_whenTasksExist_shouldReturnAverage() {
        Double expectedAverage = 8.5;
        when(tasksDataWarehouseRepository.getAverageTimeCard(TEST_USER_ID))
                .thenReturn(Optional.of(expectedAverage));

        Double result = tasksService.getAverageTimeCard(TEST_USER_ID);

        assertEquals(expectedAverage, result);
    }

    @Test
    @DisplayName("getAverageTimeCard - should throw when no completed tasks")
    void getAverageTimeCard_whenNoCompletedTasks_shouldThrow() {
        when(tasksDataWarehouseRepository.getAverageTimeCard(TEST_USER_ID))
                .thenReturn(Optional.empty());

        NoSuchElementException exception = assertThrows(NoSuchElementException.class,
                () -> tasksService.getAverageTimeCard(TEST_USER_ID));

        assertEquals("No tasks completed", exception.getMessage());
    }

    @Test
    @DisplayName("getAverageTimeCard - should throw when userId is null")
    void getAverageTimeCard_whenUserIdIsNull_shouldThrow() {
        assertThrows(IllegalArgumentException.class,
                () -> tasksService.getAverageTimeCard(null));
    }
}
