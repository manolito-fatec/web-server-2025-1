package com.manolito.dashflow.service.application;

import com.manolito.dashflow.dto.application.ApplicationToolDto;
import com.manolito.dashflow.entity.application.ApplicationTool;
import com.manolito.dashflow.repository.application.ApplicationToolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing application tools.
 * <p>
 * This service provides access to all available tools in the application.
 * </p>
 */
@RequiredArgsConstructor
@Service
public class ApplicationToolService {
    private final ApplicationToolRepository applicationToolRepository;

    /**
     * Retrieves all application tools from the database.
     * <p>
     * This method fetches all tools using the Spring Data JPA repository's built-in findAll() method.
     * </p>
     *
     * Returns a list of all application tools.
     * @see ApplicationTool
     *
     * @example
     * <pre>{@code
     * // Get all tools
     * List<ApplicationTool> tools = applicationToolService.allTools;
     * }</pre>
     */
    public List<ApplicationToolDto> getAllTools() {
        return applicationToolRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private ApplicationToolDto convertToDto(ApplicationTool tool) {
        return ApplicationToolDto.builder()
                .id(tool.getId())
                .toolName(tool.getToolName())
                .build();
    }
}