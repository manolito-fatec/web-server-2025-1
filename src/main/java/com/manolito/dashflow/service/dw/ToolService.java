package com.manolito.dashflow.service.dw;

import com.manolito.dashflow.enums.ProjectManagementTool;
import com.manolito.dashflow.handlers.tools.ToolHandler;
import com.manolito.dashflow.handlers.tools.ToolHandlerFactory;
import com.manolito.dashflow.service.EndpointService;
import com.manolito.dashflow.util.ProjectManagementToolUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ToolService implements EndpointService {

    private final ToolHandlerFactory toolHandlerFactory;

    @Override
    public void handleRequest(String requestUrl) {
        ProjectManagementTool tool = ProjectManagementToolUtils.identifyTool(requestUrl);

        if (tool == null) {
            throw new RuntimeException("Error on Identifying the Project Management Tool");
        }

        String endpointPath = requestUrl.replace(tool.getBaseUrl(), "");

        ToolHandler handler = toolHandlerFactory.getToolHandlers().get(tool);

        if (handler == null) {
            throw new RuntimeException("No handler found for tool: " + tool);
        }

        handler.handleRequest(endpointPath);
    }
}
