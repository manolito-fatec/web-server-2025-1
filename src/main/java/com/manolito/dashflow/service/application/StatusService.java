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

    public List<StatusCountDto> getStatusCountGroupByStatusByUserIdAndProjectId(Integer userId, Integer projectId) {
        List<StatusCountDto> statusCountDto = tasksDataWarehouseRepository.getStatusCountGroupByStatusByUserIdAndProjectId(
                userId, projectId
        );
        if (statusCountDto.isEmpty()) {
            throw new NoSuchElementException("No statuses found");
        }
        return statusCountDto;
    }
}
