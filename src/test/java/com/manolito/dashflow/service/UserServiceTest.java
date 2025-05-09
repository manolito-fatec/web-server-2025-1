package com.manolito.dashflow.service;

import com.manolito.dashflow.dto.dw.UserDto;
import com.manolito.dashflow.repository.application.TasksDataWarehouseRepository;
import com.manolito.dashflow.service.application.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private TasksDataWarehouseRepository dataWarehouseRepository;

    @InjectMocks
    private UserService userService;

    private final String TEST_PROJECT_ID = "Taiga123";

    @Test
    @DisplayName("getTaskCountByStatusByOperatorIdBetween - should return users when project exists")
    void shouldReturnUsersWhenProjectExists() {
        List<UserDto> expectedUsers = List.of(
                new UserDto("12345", "Cebolão"),
                new UserDto("user102030", "Omniman")
        );
        when(dataWarehouseRepository.getUsersByProjectId(TEST_PROJECT_ID))
                .thenReturn(expectedUsers);

        List<UserDto> result = userService.getUsersByProjectId(TEST_PROJECT_ID);

        assertEquals(2, result.size());
        assertEquals("12345", result.get(0).getOriginalId());
        assertEquals("Cebolão", result.get(0).getUserName());
        verify(dataWarehouseRepository).getUsersByProjectId(TEST_PROJECT_ID);
    }

    @Test
    @DisplayName("getTaskCountByStatusByOperatorIdBetween - should return empty list when no users found")
    void shouldReturnEmptyListWhenNoUsers() {
        when(dataWarehouseRepository.getUsersByProjectId(TEST_PROJECT_ID))
                .thenReturn(Collections.emptyList());

        List<UserDto> result = userService.getUsersByProjectId(TEST_PROJECT_ID);

        assertTrue(result.isEmpty());
        verify(dataWarehouseRepository).getUsersByProjectId(TEST_PROJECT_ID);
    }

    @Test
    @DisplayName("getTaskCountByStatusByOperatorIdBetween - should throw when projectId is null")
    void shouldThrowWhenProjectIdIsNull() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.getUsersByProjectId(null)
        );

        assertEquals("projectId cannot be null", exception.getMessage());
        verify(dataWarehouseRepository, never()).getUsersByProjectId(any());
    }
}