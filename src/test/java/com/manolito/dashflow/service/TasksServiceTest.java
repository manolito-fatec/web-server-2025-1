package com.manolito.dashflow.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.NoSuchElementException;
import java.util.Optional;

import com.manolito.dashflow.repository.application.TasksDataWarehouseRepository;
import com.manolito.dashflow.service.application.TasksService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TasksServiceTest {

    @Mock
    private TasksDataWarehouseRepository tasksDataWarehouseRepository;

    @InjectMocks
    private TasksService tasksService;

    private final int TEST_USER_ID = 123;
    private final LocalDate TEST_START_DATE = LocalDate.parse("2024-01-01");
    private final LocalDate TEST_END_DATE = LocalDate.parse("2025-01-01");
    @Test
    @DisplayName("Should throw NoSuchElementException when no task status counts are found")
    void getTaskCountByStatusByOperatorIdBetween_whenNoResults_shouldThrowException() {
        when(tasksDataWarehouseRepository.getTotalTasksByStatusByOperatorBetween(
                anyInt(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        assertThrows(NoSuchElementException.class, () ->
                tasksService.getTaskCountByStatusByOperatorIdBetween(TEST_USER_ID,TEST_START_DATE, TEST_END_DATE)
        );

        verify(tasksDataWarehouseRepository, times(1))
                .getTotalTasksByStatusByOperatorBetween(TEST_USER_ID, TEST_START_DATE,TEST_END_DATE);
    }



    @Test
    @DisplayName("Test when tasks are successfully retrieved and average time is calculated")
    void testGetAverageTimeCard_Success() {
        Integer userId = 1;
        Double expectedTime = 8.67;

        when(tasksDataWarehouseRepository.getAverageTimeCard(userId)).thenReturn(Optional.of(expectedTime));

        Double result = tasksService.getAverageTimeCard(userId);

        assertEquals(expectedTime, result);
        verify(tasksDataWarehouseRepository, times(1)).getAverageTimeCard(userId);
    }

    @Test
    @DisplayName("Test when no tasks are completed and NoSuchElementException is thrown")
    void testGetAverageTimeCard_NoTasksCompleted() {
        Integer userId = 2;

        when(tasksDataWarehouseRepository.getAverageTimeCard(userId)).thenReturn(Optional.empty());

        NoSuchElementException exception = assertThrows(NoSuchElementException.class,
                () -> tasksService.getAverageTimeCard(userId));

        assertEquals("No tasks completed", exception.getMessage());
        verify(tasksDataWarehouseRepository, times(1)).getAverageTimeCard(userId);
    }

}
