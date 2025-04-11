package com.manolito.dashflow.service.application;

import com.manolito.dashflow.dto.dw.StatusCountDto;
import com.manolito.dashflow.repository.application.TasksDataWarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;


@Service
@RequiredArgsConstructor
public class StatusService {
    private final TasksDataWarehouseRepository tasksDataWarehouseRepository;

    /**
     * Retrieves the count of tasks grouped by status for a specific user and project.
     * <p>
     * This method queries the data warehouse to get the number of tasks in each status
     * for the given user and project combination. Only the current version of tasks and statuses are considered.
     * </p>
     *
     * @param userId    the ID of the user to filter tasks by (must not be null)
     * @param projectId the ID of the project to filter tasks by (must not be null)
     * @return a list of {@link StatusCountDto} objects containing status names and corresponding task counts
     * @throws NoSuchElementException if no status information is found for the given user and project
     * @throws NullPointerException if either userId or projectId is null
     * @see StatusCountDto
     * @see TasksDataWarehouseRepository#getTaskCountGroupByStatusByUserIdAndProjectId(int, String)
     *
     * @example
     * <pre>{@code
     * // Get status counts for user 123 in project 456
     * List<StatusCountDto> statusCounts = statusService
     *     .getTaskCountGroupByStatusByUserIdAndProjectId(123, 456);
     * }</pre>
     */
    public List<StatusCountDto> getTaskCountGroupByStatusByUserIdAndProjectId(Integer userId, String projectId) {
        List<StatusCountDto> statusCountDto = tasksDataWarehouseRepository.getTaskCountGroupByStatusByUserIdAndProjectId(
                userId, projectId
        );
        if (projectId == null) {
            throw new NullPointerException("projectId cannot be null");
        }
        if (statusCountDto.isEmpty()) {
            throw new NoSuchElementException("No statuses found");
        }
        return statusCountDto;
    }

    /**
     * Retrieves the count of tasks grouped by status for a specific project.
     * <p>
     * This method queries the data warehouse to get the number of tasks in each status
     * for the given project. Only the current version of tasks and statuses are considered.
     * </p>
     *
     * @param projectId the ID of the project to filter tasks by (must not be null)
     * @return a list of {@link StatusCountDto} objects containing status names and corresponding task counts
     * @throws NoSuchElementException if no status information is found for the given project
     * @throws NullPointerException if projectId is null
     * @see StatusCountDto
     * @see TasksDataWarehouseRepository#getTaskCountGroupByStatusByProjectId(String)
     *
     * @example
     * <pre>{@code
     * // Get status counts for project 777
     * List<StatusCountDto> statusCounts = statusService
     *     .getTaskCountGroupByStatusByProjectId("777");
     * }</pre>
     */
    public List<StatusCountDto> getTaskCountGroupByStatusByProjectId(String projectId) {
        List<StatusCountDto> statusCountDto = tasksDataWarehouseRepository.getTaskCountGroupByStatusByProjectId(
                projectId
        );
        if (projectId == null) {
            throw new NullPointerException("projectId cannot be null");
        }
        if (statusCountDto.isEmpty()) {
            throw new NoSuchElementException("No statuses found");
        }
        return statusCountDto;
    }
}
