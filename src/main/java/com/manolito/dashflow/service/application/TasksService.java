package com.manolito.dashflow.service.application;

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

    public List<StatusCountDto> getTaskCountByStatusByOperatorIdBetween(Integer userId, LocalDate startDate, LocalDate endDate) {
        List<StatusCountDto> taskCount = tasksDataWarehouseRepository.getTotalTasksByStatusByOperatorBetween(userId, startDate, endDate);
        if (taskCount.isEmpty()) {
            throw new NoSuchElementException("No tasks found in the time period");
        }
        return taskCount;
    }
}
