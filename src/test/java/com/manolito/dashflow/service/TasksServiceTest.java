package com.manolito.dashflow.service;

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
public class TasksServiceTest {

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

}
