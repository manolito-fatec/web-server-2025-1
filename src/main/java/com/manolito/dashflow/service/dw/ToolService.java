package com.manolito.dashflow.service.dw;

import com.manolito.dashflow.enums.ProjectManagementTool;
import com.manolito.dashflow.service.EndpointService;
import com.manolito.dashflow.util.ProjectManagementToolUtils;
import org.springframework.stereotype.Service;

@Service
public class ToolService implements EndpointService {

    @Override
    public void handleRequest(String requestUrl) {
        ProjectManagementTool tool = ProjectManagementToolUtils.identifyTool(requestUrl);

        if (tool == null) {
            throw new RuntimeException("Error on Identifying the Project Management Tool");
        } else {
            // Further logic
        }
    }
}
