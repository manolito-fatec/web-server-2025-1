package com.manolito.dashflow.service.application;

import com.manolito.dashflow.dto.dw.CreatedDoneDto;
import com.manolito.dashflow.repository.application.TasksDataWarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TasksService {
    private final TasksDataWarehouseRepository tasksDataWarehouseRepository;

    /**
     * Retrieves the total number of tasks assigned to a specific operator.
     *
     * @param userId the ID of the operator to query tasks for
     * @return the total count of tasks assigned to the operator
     * @throws NoSuchElementException if no tasks are found for the operator
     */
    public Integer getTaskCountByOperatorId(Integer userId) {
        Optional<Integer> taskCount = tasksDataWarehouseRepository.getTotalTasksByOperator(userId);
        if (taskCount.isEmpty()) {
            throw new NoSuchElementException("No tasks found");
        }
        return taskCount.get();
    }

    /**
     * Retrieves the total number of tasks assigned to a specific operator within a given date range.
     *
     * @param userId the ID of the operator to query tasks for
     * @param startDate the start date of the period (inclusive)
     * @param endDate the end date of the period (inclusive)
     * @return the total count of tasks assigned to the operator during the specified period
     * @throws NoSuchElementException if no tasks are found within the given date range
     * @throws IllegalArgumentException if the start date is after the end date
     */
    public Integer getTaskCountByOperatorIdBetween(Integer userId, LocalDate startDate, LocalDate endDate) {
        Optional<Integer> taskCount = tasksDataWarehouseRepository.getTotalTasksByOperatorBetween(userId, startDate, endDate);
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date is after end date");
        }
        if (taskCount.isEmpty()) {
            throw new NoSuchElementException("No tasks found in the time period");
        }
        return taskCount.get();
    }

    /**
     * Retrieves a list of task counts grouped by their status for a specific operator within a given date range.
     * Each element in the list contains the task status and the corresponding count.
     *
     * @param userId the ID of the operator to query tasks for
     * @param startDate the start date of the period (inclusive)
     * @param endDate the end date of the period (inclusive)
     * @return a list of {@link CreatedDoneDto} objects representing task counts by status
     * @throws NoSuchElementException if no tasks are found within the given date range
     * @throws IllegalArgumentException if the start date is after the end date
     */
    public List<CreatedDoneDto> getTaskCountByStatusByOperatorIdBetween(Integer userId, LocalDate startDate, LocalDate endDate) {
        List<CreatedDoneDto> taskCount = tasksDataWarehouseRepository.getTotalTasksByStatusByOperatorBetween(userId, startDate, endDate);
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date is after end date");
        }
        if (taskCount.isEmpty()) {
            throw new NoSuchElementException("No tasks found in the time period");
        }
        return taskCount;
    }

    /**
     * Retrieves a list of started and completed tasks for a specific project within a given date range.
     *
     * @param projectId the ID of the project to query tasks for
     * @param startDate the start date of the period (inclusive)
     * @param endDate the end date of the period (inclusive)
     * @return a list of {@link CreatedDoneDto} objects representing task
     * @throws NoSuchElementException if no tasks are found within the given date range
     * @throws NullPointerException if the given project ID is null
     * @throws IllegalArgumentException if the start date is after the end date
     */
    public CreatedDoneDto getCreatedAndCompletedTaskCountByProjectBetween(String projectId, LocalDate startDate, LocalDate endDate) {
        Optional<CreatedDoneDto> taskCount = tasksDataWarehouseRepository.getAllCreatedAndCompletedTasksByProjectBetween(projectId, startDate, endDate);
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date is after end date");
        }
        if (projectId == null) {
            throw new NullPointerException("Project ID cannot be empty or null");
        }
        if (taskCount.isEmpty()) {
            throw new NoSuchElementException("No tasks found in the time period");
        }
        return taskCount.get();
    }

    /**
     * Retrieves the average time a user takes to complete their tasks, calculating the average amount of tasks done by week.
     *
     * @param userId the ID of the user to query tasks for
     * @return average task completion time by the user, in the following format: '0.0' (days)
     * @throws NoSuchElementException if no tasks are found
     * @throws IllegalArgumentException if user ID is null
     */
    public Double getAverageTimeCard(Integer userId) {
        Optional<Double> averageTimeCard = tasksDataWarehouseRepository.getAverageTimeCard(userId);
        if (userId == null) {
            throw new IllegalArgumentException("User id is null");
        }
        if (averageTimeCard.isPresent()) {
            return averageTimeCard.get();
        }
        throw new NoSuchElementException("No tasks completed");
    }


    /**
     * Retrieves the total cards assigned to manager from all projects
     *
     * @param userId the ID of the user logged in application
     * @return total cards assigned to manager
     * @throws NoSuchElementException if no cards are assigned to manager
     */
    public Integer getTotalCardsForManager(Integer userId) {
        Optional<Integer> cardsCount = tasksDataWarehouseRepository.getTotalCardsForManager(userId);
        if (cardsCount.isEmpty()) {
            throw new NoSuchElementException("No cards found for this manager");
        }
        return cardsCount.get();
    }
}
