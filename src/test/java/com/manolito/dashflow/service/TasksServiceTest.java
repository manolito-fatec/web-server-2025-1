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

@DisplayName("TasksService")
@ExtendWith(MockitoExtension.class)
class TasksServiceTest {

    @Mock
    private TasksDataWarehouseRepository tasksDataWarehouseRepository;

    @InjectMocks
    private TasksService tasksService;

    @Test
    void testGetAverageTimeCard_Success() {
        Integer userId = 1;
        Double expectedTime = 8.67;

        when(tasksDataWarehouseRepository.getAverageTimeCard(userId)).thenReturn(Optional.of(expectedTime));

        Double result = tasksService.getAverageTimeCard(userId);

        assertEquals(expectedTime, result);
        verify(tasksDataWarehouseRepository, times(1)).getAverageTimeCard(userId);
    }

    @Test
    void testGetAverageTimeCard_NoTasksCompleted() {
        Integer userId = 2;

        when(tasksDataWarehouseRepository.getAverageTimeCard(userId)).thenReturn(Optional.empty());

        NoSuchElementException exception = assertThrows(NoSuchElementException.class,
                () -> tasksService.getAverageTimeCard(userId));

        assertEquals("No tasks completed", exception.getMessage());
        verify(tasksDataWarehouseRepository, times(1)).getAverageTimeCard(userId);
    }
}

