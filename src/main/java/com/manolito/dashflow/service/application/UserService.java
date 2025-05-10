package com.manolito.dashflow.service.application;

import com.manolito.dashflow.dto.dw.UserDto;
import com.manolito.dashflow.dto.dw.UserTableDto;
import com.manolito.dashflow.repository.application.TasksDataWarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final TasksDataWarehouseRepository dataWarehouseRepository;
    private static final int DEFAULT_PAGE_SIZE = 10;

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
    public List<UserDto> getUsersByProjectId(String projectId) {
        if (projectId == null) {
            throw new IllegalArgumentException("projectId cannot be null");
        }

        return dataWarehouseRepository.getUsersByProjectId(projectId);
    }


    /**
     * Retrieves paginated list of application users with their associated accounts, tools and projects.
     * <p>
     * This method excludes users with admin role (role_id = 3) and provides pagination support.
     * Uses default page size of 10 if not specified.
     * </p>
     *
     * @param page     the page number (defaults to 1)
     * @return Page of {@link UserTableDto} containing user information
     * @throws IllegalArgumentException if page or pageSize are invalid
     *
     * @example
     * <pre>{@code
     * // Get first page with default size (10 users)
     * Page<UserTableDto> users = userService.getUsersPaginated(1);
     *
     * // Get second page with 20 users per page
     * Page<UserTableDto> users = userService.getUsersPaginated(2, 20);
     * }</pre>
     */
    public Page<UserTableDto> getUsersPaginated(int page) {
        return getUsersPaginated(page, DEFAULT_PAGE_SIZE);
    }

    /**
     * Retrieves paginated list of application users with their associated accounts, tools and projects.
     * <p>
     * This method excludes users with admin role (role_id = 3) and provides pagination support.
     * This method uses a given page size.
     * </p>
     *
     * @param page     the page number (defaults to 1)
     * @param pageSize the number of items per page
     * @return Page of {@link UserTableDto} containing user information
     * @throws IllegalArgumentException if page or pageSize are invalid
     *
     * @example
     * <pre>{@code
     * // Get second page with 20 users per page
     * Page<UserTableDto> users = userService.getUsersPaginated(2, 20);
     * }</pre>
     */
    public Page<UserTableDto> getUsersPaginated(int page, int pageSize) {
        if (page < 1) {
            throw new IllegalArgumentException("Page must be greater than 0");
        }
        if (pageSize < 1) {
            throw new IllegalArgumentException("Page size must be greater than 0");
        }

        List<UserTableDto> users = dataWarehouseRepository.getUsersPaginated(page, pageSize);
        int totalUsers = dataWarehouseRepository.countAllApplicationUsers();

        return new PageImpl<>(
                users,
                PageRequest.of(page - 1, pageSize),
                totalUsers
        );
    }
}
