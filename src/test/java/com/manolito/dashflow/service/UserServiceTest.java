package com.manolito.dashflow.service;

import com.manolito.dashflow.dto.dw.UserDto;
import com.manolito.dashflow.dto.dw.UserProjectDto;
import com.manolito.dashflow.dto.dw.UserTableDto;
import com.manolito.dashflow.repository.application.TasksDataWarehouseRepository;
import com.manolito.dashflow.service.application.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;

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
    private final int TEST_PAGE = 1;
    private final int TEST_PAGE_SIZE = 10;
    private final int TOTAL_USERS = 25;

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

    @Test
    @DisplayName("getUsersPaginated - should return paginated users when users exist")
    void getUsersPaginated_shouldReturnPaginatedUsers() {
        List<UserTableDto> mockUsers = List.of(
                createMockUserTableDto("1", "User1"),
                createMockUserTableDto("2", "User2")
        );

        when(dataWarehouseRepository.getUsersPaginated(TEST_PAGE, TEST_PAGE_SIZE))
                .thenReturn(mockUsers);
        when(dataWarehouseRepository.countAllApplicationUsers()).thenReturn(TOTAL_USERS);

        Page<UserTableDto> result = userService.getUsersPaginated(TEST_PAGE);

        assertEquals(2, result.getContent().size());
        assertEquals(TOTAL_USERS, result.getTotalElements());
        assertEquals(TEST_PAGE_SIZE, result.getSize());
        verify(dataWarehouseRepository).getUsersPaginated(TEST_PAGE, TEST_PAGE_SIZE);
        verify(dataWarehouseRepository).countAllApplicationUsers();
    }

    @Test
    @DisplayName("getUsersPaginated - should return empty page when no users exist")
    void getUsersPaginated_shouldReturnEmptyPage() {
        when(dataWarehouseRepository.getUsersPaginated(TEST_PAGE, TEST_PAGE_SIZE))
                .thenReturn(Collections.emptyList());
        when(dataWarehouseRepository.countAllApplicationUsers()).thenReturn(0);

        Page<UserTableDto> result = userService.getUsersPaginated(TEST_PAGE);

        assertTrue(result.getContent().isEmpty());
        assertEquals(0, result.getTotalElements());
        verify(dataWarehouseRepository).getUsersPaginated(TEST_PAGE, TEST_PAGE_SIZE);
        verify(dataWarehouseRepository).countAllApplicationUsers();
    }

    @Test
    @DisplayName("getUsersPaginated - should throw when page is invalid")
    void getUsersPaginated_shouldThrowForInvalidPage() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.getUsersPaginated(0)
        );

        assertEquals("Page must be greater than 0", exception.getMessage());
        verify(dataWarehouseRepository, never()).getUsersPaginated(anyInt(), anyInt());
    }

    @Test
    @DisplayName("getUsersPaginated - should throw when pageSize is invalid")
    void getUsersPaginated_shouldThrowForInvalidPageSize() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.getUsersPaginated(1, 0)
        );

        assertEquals("Page size must be greater than 0", exception.getMessage());
        verify(dataWarehouseRepository, never()).getUsersPaginated(anyInt(), anyInt());
    }

    @Test
    @DisplayName("getUsersPaginated - should use custom page size when provided")
    void getUsersPaginated_shouldUseCustomPageSize() {
        int customPageSize = 20;
        List<UserTableDto> mockUsers = List.of(createMockUserTableDto("1", "User1"));

        when(dataWarehouseRepository.getUsersPaginated(TEST_PAGE, customPageSize))
                .thenReturn(mockUsers);
        when(dataWarehouseRepository.countAllApplicationUsers()).thenReturn(TOTAL_USERS);

        Page<UserTableDto> result = userService.getUsersPaginated(TEST_PAGE, customPageSize);

        assertEquals(1, result.getContent().size());
        assertEquals(customPageSize, result.getSize());
        verify(dataWarehouseRepository).getUsersPaginated(TEST_PAGE, customPageSize);
    }

    private UserTableDto createMockUserTableDto(String id, String name) {
        return UserTableDto.builder()
                .userId(id)
                .userName(name)
                .userRole("USER")
                .toolName("Tool")
                .userEmail("email")
                .toolId(1)
                .projectId("PRJ333")
                .projectName("TokyoDrift")
                .createdAt(null)
                .build();
    }

    @Test
    @DisplayName("getProjectUsersByManagerId - should return users when manager exists")
    void getProjectUsersByManagerId_whenManagerExists_shouldReturnUsers() {
        String managerId = "777";
        List<UserProjectDto> expectedUsers = List.of(
                UserProjectDto.builder()
                        .userId("123")
                        .userName("Papaleguas")
                        .projectId("P100")
                        .projectName("Alpha")
                        .build(),
                UserProjectDto.builder()
                        .userId("456")
                        .userName("Sharkboy")
                        .projectId("P200")
                        .projectName("Beta")
                        .build()
        );

        when(dataWarehouseRepository.getProjectUsersByManagerId(managerId))
                .thenReturn(expectedUsers);

        List<UserProjectDto> result = userService.getProjectUsersByManagerId(managerId);

        assertEquals(2, result.size());
        assertEquals("123", result.get(0).getUserId());
        assertEquals("Papaleguas", result.get(0).getUserName());
        assertEquals("P100", result.get(0).getProjectId());
        assertEquals("Alpha", result.get(0).getProjectName());
        verify(dataWarehouseRepository).getProjectUsersByManagerId(managerId);
    }

    @Test
    @DisplayName("getProjectUsersByManagerId - should return empty list when no users found")
    void getProjectUsersByManagerId_whenNoUsers_shouldReturnEmptyList() {
        String managerId = "888";
        when(dataWarehouseRepository.getProjectUsersByManagerId(managerId))
                .thenReturn(Collections.emptyList());

        List<UserProjectDto> result = userService.getProjectUsersByManagerId(managerId);

        assertTrue(result.isEmpty());
        verify(dataWarehouseRepository).getProjectUsersByManagerId(managerId);
    }

    @Test
    @DisplayName("getProjectUsersByManagerId - should throw when managerId is null")
    void getProjectUsersByManagerId_whenManagerIdIsNull_shouldThrow() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.getProjectUsersByManagerId(null)
        );

        assertEquals("managerId cannot be null", exception.getMessage());
        verify(dataWarehouseRepository, never()).getProjectUsersByManagerId(any());
    }

    @Test
    @DisplayName("getProjectUsersByManagerId - should return users with project information")
    void getProjectUsersByManagerId_shouldReturnProjectInformation() {
        String managerId = "999";
        List<UserProjectDto> expectedUsers = List.of(
                UserProjectDto.builder()
                        .userId("111")
                        .userName("Membro Gamer")
                        .projectId("GAM123")
                        .projectName("Projeto Gamer")
                        .build()
        );

        when(dataWarehouseRepository.getProjectUsersByManagerId(managerId))
                .thenReturn(expectedUsers);

        List<UserProjectDto> result = userService.getProjectUsersByManagerId(managerId);

        assertEquals(1, result.size());
        assertEquals("GAM123", result.get(0).getProjectId());
        assertEquals("Projeto Gamer", result.get(0).getProjectName());
        verify(dataWarehouseRepository).getProjectUsersByManagerId(managerId);
    }
}