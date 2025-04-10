package com.manolito.dashflow.service.application;

import com.manolito.dashflow.dto.dw.CreatedDoneDto;
import com.manolito.dashflow.dto.dw.StatusCountDto;
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
     */
    public Integer getTaskCountByOperatorIdBetween(Integer userId, LocalDate startDate, LocalDate endDate) {
        Optional<Integer> taskCount = tasksDataWarehouseRepository.getTotalTasksByOperatorBetween(userId, startDate, endDate);
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
     */
    public List<CreatedDoneDto> getTaskCountByStatusByOperatorIdBetween(Integer userId, LocalDate startDate, LocalDate endDate) {
        List<CreatedDoneDto> taskCount = tasksDataWarehouseRepository.getTotalTasksByStatusByOperatorBetween(userId, startDate, endDate);
        if (taskCount.isEmpty()) {
            throw new NoSuchElementException("No tasks found in the time period");
        }
        return taskCount;
    }

      /**
     * Busca a média de tempo que o usuário leva para completar suas tasks, calculando a média de tasks feitas por semana.
     *
     * @param userId O id do usuário buscado para o cálculo.
     * @return valor da média calculada, em formato '0.0'
     * @throws 'No tasks completed'
     */

    public Double getAverageTimeCard(Integer userId) {
        Optional<Double> averageTimeCard = tasksDataWarehouseRepository.getAverageTimeCard(userId);
        if (averageTimeCard.isPresent()) {
            return averageTimeCard.get();
        }
        throw new NoSuchElementException("No tasks completed");
    }

    public Integer getTotalCardsForManager(Integer userId) {
        Optional<Integer> cardsCount = tasksDataWarehouseRepository.getTotalCardsForManager(userId);
        if (cardsCount.isEmpty()) {
            throw new NoSuchElementException("No cards found for this manager");
        }
        return cardsCount.get();
    }
}
