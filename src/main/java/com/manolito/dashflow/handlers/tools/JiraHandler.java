package com.manolito.dashflow.handlers.tools;

import com.manolito.dashflow.enums.JiraEndpoints;
import com.manolito.dashflow.service.dw.JiraService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JiraHandler implements ToolHandler {

    private final JiraService jiraService;

    @Override
    public void handleRequest(String endpointPath) {

        JiraEndpoints object = JiraEndpoints.identifyObject(endpointPath);

        if (object == null) {
            throw new RuntimeException("Error on Identifying the Taiga Object");
        }

        switch (object) {
            case USERS:
                jiraService.handleUsers();
                break;
            default:
                throw new RuntimeException("Unsupported Jir Object");
        }
    }
}
