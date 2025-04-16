package com.manolito.dashflow.handlers.tools;

import com.manolito.dashflow.enums.TaigaEndpoints;
import com.manolito.dashflow.service.dw.TaigaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TaigaHandler implements ToolHandler {

    private final TaigaService taigaService;

    @Override
    public void handleRequest(String endpointPath) {
        TaigaEndpoints object = TaigaEndpoints.identifyObject(endpointPath);

        if (object == null) {
            throw new RuntimeException("Error on Identifying the Taiga Object");
        }

        switch (object) {
            case PROJECTS:
                taigaService.handleProjects();
                break;
            case USER_STORIES:
                taigaService.handleUserStories();
                break;
            case TASKS:
                taigaService.handleTasks();
                break;
            case ISSUES:
                taigaService.handleIssues();
                break;
            case EPICS:
                taigaService.handleEpics();
                break;
            default:
                throw new RuntimeException("Unsupported Taiga Object");
        }
    }
}
