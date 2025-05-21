package com.manolito.dashflow.service.application;

import com.manolito.dashflow.dto.dw.ProjectDto;
import com.manolito.dashflow.dto.dw.ProjectTableDto;
import com.manolito.dashflow.dto.dw.TaskProjectDto;
import com.manolito.dashflow.repository.application.TasksDataWarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * Service for handling project-related operations.
 */
@Service
@RequiredArgsConstructor
public class ProjectsService {
    private final TasksDataWarehouseRepository tasksDataWarehouseRepository;

    /**
     * Retrieves the count of projects associated with a specific user.
     * <p>
     * This method queries the data warehouse to get the number of projects
     * where the specified user has tasks assigned.
     * </p>
     *
     * @param userId the ID of the user to query projects for (must not be null)
     * @return the count of projects for the specified user
     * @throws NoSuchElementException if no projects are found for the given user ID
     * @throws IllegalArgumentException if userId is null
     *
     * @example
     * <pre>{@code
     * // Get project count for user with ID 123
     * int count = projectsService.getProjectsCountByUserId(123);
     * }</pre>
     */
    public Integer getProjectsCountByUserId(Integer userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        Optional<Integer> projectCount = tasksDataWarehouseRepository.getTotalProjectsByUserId(userId);
        if (projectCount.isEmpty()) {
            throw new NoSuchElementException("No projects found for user id " + userId);
        }
        return projectCount.get();
    }

    /**
     * Retrieves the amount of projects that exist in the platform.
     *
     * @return a list of {@link TaskProjectDto} objects representing task counts and their respective projects
     */
    public Optional<Integer> getProjectCount() {
        return tasksDataWarehouseRepository.getProjectCount();
    }

    /**
     * Retrieves all current projects associated with a specific tool.
     * <p>
     * This method queries the data warehouse for the projects that are associated with the given tool ID.
     * @param toolId the ID of the tool to filter projects by (must not be null)
     * @return a list of {@link ProjectDto} objects containing project information
     * @throws IllegalArgumentException if toolId is null
     *
     * @see ProjectDto
     *
     * @example
     * <pre>{@code
     * // Get projects using tool ID 5
     * List<ProjectDto> projects = projectService.getProjectsByTool(5);
     * }</pre>
     */
    public List<ProjectDto> getProjectsByTool(Integer toolId) {
        if (toolId == null) {
            throw new IllegalArgumentException("Tool ID cannot be null");
        }
        return tasksDataWarehouseRepository.getProjectsByTool(toolId);
    }

    /**
     * Retrieves paginated list of projects with their basic information.
     * <p>
     * This method provides pagination support for projects with their IDs, names, manager information,
     * operator counts, and associated tools. The pagination is 1-based.
     * </p>
     *
     * @param page     the page number (defaults to 1)
     * @param pageSize the number of items per page
     * @return Page of {@link ProjectTableDto} containing project information
     * @throws IllegalArgumentException if page or pageSize are less than 1
     *
     * @example
     * <pre>{@code
     * // Get first page with 20 projects per page
     * Page<ProjectTableDto> projects = projectService.getProjectsPaginated(1, 20);
     * }</pre>
     */
    public Page<ProjectTableDto> getProjectsPaginated(int page, int pageSize) {
        if (page < 1) {
            throw new IllegalArgumentException("Page must be greater than 0");
        }
        if (pageSize < 1) {
            throw new IllegalArgumentException("Page size must be greater than 0");
        }

        List<ProjectTableDto> projects = tasksDataWarehouseRepository.getProjectsPaginated(page, pageSize);
        int totalProjects = tasksDataWarehouseRepository.countAllProjects();

        return new PageImpl<>(
                projects,
                PageRequest.of(page - 1, pageSize),
                totalProjects
        );
    }
}
