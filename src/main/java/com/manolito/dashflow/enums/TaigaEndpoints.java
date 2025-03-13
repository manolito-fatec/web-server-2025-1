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

    USER_STORY_STATUSES("/api/v1/userstory-statuses"),

    EPICS("/api/v1/epics"),

    ROLES("/api/v1/roles"),

    PROJECT_MEMBERS("/api/v1/projects/{project_id}/members");

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