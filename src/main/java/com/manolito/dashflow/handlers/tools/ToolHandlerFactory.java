package com.manolito.dashflow.handlers.tools;

import com.manolito.dashflow.enums.ProjectManagementTool;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Getter
@Component
public class ToolHandlerFactory {

    private final Map<ProjectManagementTool, ToolHandler> toolHandlers = new HashMap<>();

    @Autowired
    public ToolHandlerFactory(TaigaHandler taigaHandler) {
        toolHandlers.put(ProjectManagementTool.TAIGA, taigaHandler);
    }

}
