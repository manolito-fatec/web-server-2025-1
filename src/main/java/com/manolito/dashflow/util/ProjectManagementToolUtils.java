package com.manolito.dashflow.util;

import com.manolito.dashflow.enums.ProjectManagementTool;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ProjectManagementToolUtils {

    private static final Map<String, ProjectManagementTool> toolMapping = new HashMap<>();

    static {
        for (ProjectManagementTool tool : ProjectManagementTool.values()) {
            toolMapping.put(tool.name().toLowerCase(), tool);
        }
    }

    /**
     * Identifies the project management tool based on the request URL.
     *
     * @param requestUrl The URL string from the request.
     * @return The corresponding ProjectManagementTool or null if no match is found.
     */
    public static ProjectManagementTool identifyTool(String requestUrl) {
        for (Map.Entry<String, ProjectManagementTool> entry : toolMapping.entrySet()) {
            if (requestUrl.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }
}
