package com.manolito.dashflow.service.application;

import com.manolito.dashflow.dto.dw.TaskProjectDto;
import com.manolito.dashflow.repository.application.TasksDataWarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
}
