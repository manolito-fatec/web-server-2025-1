package com.manolito.dashflow.service;

import com.manolito.dashflow.dto.dw.ProjectDto;
import com.manolito.dashflow.repository.application.TasksDataWarehouseRepository;
import com.manolito.dashflow.service.application.ProjectsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProjectsServiceTest {

    @Mock
    private TasksDataWarehouseRepository tasksDataWarehouseRepository;

    @InjectMocks
    private ProjectsService projectsService;

    private final Integer TEST_USER_ID = 123;

    @Test
    @DisplayName("getProjectsCountByUserId - should return count when projects exist")
    void getProjectsCountByUserId_whenProjectsExist_shouldReturnCount() {
        int expectedCount = 3;
        when(tasksDataWarehouseRepository.getTotalProjectsByUserId(TEST_USER_ID))
                .thenReturn(Optional.of(expectedCount));

        Integer result = projectsService.getProjectsCountByUserId(TEST_USER_ID);

        assertEquals(expectedCount, result);
        verify(tasksDataWarehouseRepository).getTotalProjectsByUserId(TEST_USER_ID);
    }

    @Test
    @DisplayName("getProjectsCountByUserId - should throw when no projects found")
    void getProjectsCountByUserId_whenNoProjects_shouldThrow() {
        when(tasksDataWarehouseRepository.getTotalProjectsByUserId(TEST_USER_ID))
                .thenReturn(Optional.empty());

        NoSuchElementException exception = assertThrows(NoSuchElementException.class,
                () -> projectsService.getProjectsCountByUserId(TEST_USER_ID));

        assertEquals("No projects found for user id " + TEST_USER_ID, exception.getMessage());
        verify(tasksDataWarehouseRepository).getTotalProjectsByUserId(TEST_USER_ID);
    }

    @Test
    @DisplayName("getProjectsCountByUserId - should throw when userId is null")
    void getProjectsCountByUserId_whenUserIdIsNull_shouldThrow() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> projectsService.getProjectsCountByUserId(null));

        assertEquals("User ID cannot be null", exception.getMessage());

        verify(tasksDataWarehouseRepository, never())
                .getTotalProjectsByUserId(anyInt());
    }

    @Test
    @DisplayName("getProjectCount - should return count when projects exist")
    void getProjectCount_whenProjectsExist_shouldReturnCount() {
        int expectedCount = 5;
        when(tasksDataWarehouseRepository.getProjectCount())
                .thenReturn(Optional.of(expectedCount));

        Optional<Integer> result = projectsService.getProjectCount();

        assertTrue(result.isPresent());
        assertEquals(expectedCount, result.get());
        verify(tasksDataWarehouseRepository).getProjectCount();
    }

    @Test
    @DisplayName("getProjectCount - should return empty optional when no projects exist")
    void getProjectCount_whenNoProjects_shouldReturnEmptyOptional() {
        when(tasksDataWarehouseRepository.getProjectCount())
                .thenReturn(Optional.empty());

        Optional<Integer> result = projectsService.getProjectCount();

        assertFalse(result.isPresent());
        verify(tasksDataWarehouseRepository).getProjectCount();
    }

    @Test
    @DisplayName("getProjectsByTool - should return projects when tool exists")
    void getProjectsByTool_whenToolExists_shouldReturnProjects() {
        Integer toolId = 5;
        List<ProjectDto> expectedProjects = List.of(
                new ProjectDto("12345", "Super projeto taiga"),
                new ProjectDto("7777777", "Sete")
        );

        when(tasksDataWarehouseRepository.getProjectsByTool(toolId))
                .thenReturn(expectedProjects);

        List<ProjectDto> result = projectsService.getProjectsByTool(toolId);

        assertEquals(expectedProjects.size(), result.size());
        assertEquals(expectedProjects.get(0).getOriginalId(), result.get(0).getOriginalId());
        verify(tasksDataWarehouseRepository).getProjectsByTool(toolId);
    }

    @Test
    @DisplayName("getProjectsByTool - should return empty list when no projects exist for tool")
    void getProjectsByTool_whenNoProjects_shouldReturnEmptyList() {
        Integer toolId = 5;
        when(tasksDataWarehouseRepository.getProjectsByTool(toolId))
                .thenReturn(Collections.emptyList());

        List<ProjectDto> result = projectsService.getProjectsByTool(toolId);

        assertTrue(result.isEmpty());
        verify(tasksDataWarehouseRepository).getProjectsByTool(toolId);
    }

    @Test
    @DisplayName("getProjectsByTool - should throw when toolId is null")
    void getProjectsByTool_whenToolIdIsNull_shouldThrow() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> projectsService.getProjectsByTool(null)
        );

        assertEquals("Tool ID cannot be null", exception.getMessage());
        verify(tasksDataWarehouseRepository, never()).getProjectsByTool(anyInt());
    }
}