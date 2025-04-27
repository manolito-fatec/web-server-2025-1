package com.manolito.dashflow.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TaigaEndpoints{

    PROJECTS("/api/v1/projects"),

    USER_STORIES("/api/v1/userstories"),

    TASKS("/api/v1/tasks"),

    ISSUES("/api/v1/issues"),

    ISSUE_TYPES("/api/v1/issue-types"),

    ISSUE_PRIORITY("/api/v1/priorities"),

    ISSUE_SEVERITY("/api/v1/severities"),

    EPICS("/api/v1/epics"),

    ROLES("/api/v1/roles");

    private final String path;
    
    public static TaigaEndpoints identifyObject(String endpointPath) {
        for (TaigaEndpoints endpoint : values()) {
            if (endpointPath.startsWith(endpoint.getPath())) {
                return endpoint;
            }
        }
        return null;
    }
}
