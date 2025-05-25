package com.manolito.dashflow.service;

import com.manolito.dashflow.dto.dw.ProjectDto;
import com.manolito.dashflow.dto.dw.ProjectTableDto;
import com.manolito.dashflow.repository.application.TasksDataWarehouseRepository;
import com.manolito.dashflow.service.application.ProjectsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;

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

    @Test
    @DisplayName("getProjectsPaginated - should return paginated projects when valid parameters")
    void getProjectsPaginated_whenValidParameters_shouldReturnPaginatedProjects() {
        int page = 1;
        int pageSize = 10;
        List<ProjectTableDto> mockProjects = List.of(
                ProjectTableDto.builder()
                        .projectId("1")
                        .projectName("Project 1")
                        .managerName("Manager 1")
                        .operatorCount(5)
                        .toolId(1)
                        .build(),
                ProjectTableDto.builder()
                        .projectId("2")
                        .projectName("Project 2")
                        .managerName("Manager 2")
                        .operatorCount(3)
                        .toolId(2)
                        .build()
        );
        int totalProjects = 2;
        when(tasksDataWarehouseRepository.getProjectsPaginated(page, pageSize))
                .thenReturn(mockProjects);
        when(tasksDataWarehouseRepository.countAllProjects())
                .thenReturn(totalProjects);

        Page<ProjectTableDto> result = projectsService.getProjectsPaginated(page, pageSize);

        assertEquals(mockProjects.size(), result.getContent().size());
        assertEquals(totalProjects, result.getTotalElements());
        assertEquals(pageSize, result.getSize());
        assertEquals(page, result.getNumber() + 1); // PageImpl uses 0-based index

        verify(tasksDataWarehouseRepository).getProjectsPaginated(page, pageSize);
        verify(tasksDataWarehouseRepository).countAllProjects();
    }

    @Test
    @DisplayName("getProjectsPaginated - should throw when page is less than 1")
    void getProjectsPaginated_whenPageLessThan1_shouldThrow() {
        int invalidPage = 0;
        int pageSize = 10;

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> projectsService.getProjectsPaginated(invalidPage, pageSize)
        );

        assertEquals("Page must be greater than 0", exception.getMessage());
        verify(tasksDataWarehouseRepository, never()).getProjectsPaginated(anyInt(), anyInt());
        verify(tasksDataWarehouseRepository, never()).countAllProjects();
    }

    @Test
    @DisplayName("getProjectsPaginated - should throw when pageSize is less than 1")
    void getProjectsPaginated_whenPageSizeLessThan1_shouldThrow() {
        int page = 1;
        int invalidPageSize = 0;

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> projectsService.getProjectsPaginated(page, invalidPageSize)
        );

        assertEquals("Page size must be greater than 0", exception.getMessage());
        verify(tasksDataWarehouseRepository, never()).getProjectsPaginated(anyInt(), anyInt());
        verify(tasksDataWarehouseRepository, never()).countAllProjects();
    }

    @Test
    @DisplayName("getProjectsPaginated - should return empty page when no projects exist")
    void getProjectsPaginated_whenNoProjects_shouldReturnEmptyPage() {
        int page = 1;
        int pageSize = 10;
        List<ProjectTableDto> emptyList = Collections.emptyList();
        int totalProjects = 0;

        when(tasksDataWarehouseRepository.getProjectsPaginated(page, pageSize))
                .thenReturn(emptyList);
        when(tasksDataWarehouseRepository.countAllProjects())
                .thenReturn(totalProjects);

        Page<ProjectTableDto> result = projectsService.getProjectsPaginated(page, pageSize);

        assertTrue(result.getContent().isEmpty());
        assertEquals(0, result.getTotalElements());
        verify(tasksDataWarehouseRepository).getProjectsPaginated(page, pageSize);
        verify(tasksDataWarehouseRepository).countAllProjects();
    }
}