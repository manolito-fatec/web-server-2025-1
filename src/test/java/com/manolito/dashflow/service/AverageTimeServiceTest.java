package com.manolito.dashflow.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.NoSuchElementException;
import java.util.Optional;

import com.manolito.dashflow.repository.application.AverageTimeRepository;
import com.manolito.dashflow.service.application.AverageTimeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AverageTimeServiceTest {

    @Mock
    private AverageTimeRepository averageTimeRepository;

    @InjectMocks
    private AverageTimeService averageTimeService;

    @Test
    void testGetAverageTimeCard_Success() {
        Integer userId = 1;
        Double expectedTime = 5.5;

        when(averageTimeRepository.getAverageTimeCard(userId)).thenReturn(Optional.of(expectedTime));

        Double result = averageTimeService.getAverageTimeCard(userId);

        assertEquals(expectedTime, result);
        verify(averageTimeRepository, times(1)).getAverageTimeCard(userId);
    }

    @Test
    void testGetAverageTimeCard_NoTasksCompleted() {
        Integer userId = 2;

        when(averageTimeRepository.getAverageTimeCard(userId)).thenReturn(Optional.empty());

        NoSuchElementException exception = assertThrows(NoSuchElementException.class,
                () -> averageTimeService.getAverageTimeCard(userId));

        assertEquals("No tasks completed", exception.getMessage());
        verify(averageTimeRepository, times(1)).getAverageTimeCard(userId);
    }
}

