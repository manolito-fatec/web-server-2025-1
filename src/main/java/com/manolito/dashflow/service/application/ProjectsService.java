package com.manolito.dashflow.service.application;

import com.manolito.dashflow.repository.application.TasksDataWarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProjectsService {
    private final TasksDataWarehouseRepository tasksDataWarehouseRepository;

    public Integer getProjectsCountByUserId(Integer userId) {
        Optional<Integer> projectCount = tasksDataWarehouseRepository.getTotalProjectsByUserId(userId);
        if (projectCount.isEmpty()) {
            throw new NoSuchElementException("No projects found for user id " + userId);
        }
        return projectCount.get();
    }
}
