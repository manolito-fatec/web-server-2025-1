package com.manolito.dashflow.service.application;

import com.manolito.dashflow.dto.dw.UserDto;
import com.manolito.dashflow.repository.application.TasksDataWarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final TasksDataWarehouseRepository dataWarehouseRepository;

    /**
     * Retrieves users with their task counts filtered by project ID.
     * <p>
     * This method queries the data warehouse for current users associated with the specified project.
     * </p>
     *
     * @param projectId the ID of the project to filter users by (must not be null)
     * @return list of {@link UserDto} containing user information
     * @throws IllegalArgumentException if projectId is null
     *
     * @example
     * <pre>{@code
     * // Get users for project "45678"
     * List<UserDto> users = userService.getTaskCountByStatusByOperatorIdBetween("45678");
     * }</pre>
     */
    public List<UserDto> getTaskCountByStatusByOperatorIdBetween(String projectId) {
        if (projectId == null) {
            throw new IllegalArgumentException("projectId cannot be null");
        }

        return dataWarehouseRepository.getUsersByProjectId(projectId);
    }
}
