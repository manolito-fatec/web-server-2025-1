package com.manolito.dashflow.service.application;

import com.manolito.dashflow.repository.application.TasksDataWarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TasksService {
    private final TasksDataWarehouseRepository tasksDataWarehouseRepository;

    public Integer getTaskCountByOperatorId(Integer userId) {
        Optional<Integer> taskCount = tasksDataWarehouseRepository.getTotalTasksByOperator(userId);
        if (taskCount.isEmpty()) {
            throw new NoSuchElementException("No tasks found");
        }
        return taskCount.get();
    }

    public Integer getTaskCountByOperatorIdBetween(Integer userId, LocalDate startDate, LocalDate endDate) {
        Optional<Integer> taskCount = tasksDataWarehouseRepository.getTotalTasksByOperatorBetween(userId, startDate, endDate);
        if (taskCount.isEmpty()) {
            throw new NoSuchElementException("No tasks found in the time period");
        }
        return taskCount.get();
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
}
