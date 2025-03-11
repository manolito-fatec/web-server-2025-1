package com.manolito.dashflow.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TaigaEndpoints {

    PROJECTS("/api/v1/projects"),
    USER_STORIES("/api/v1/userstories"),
    TASKS("/api/v1/tasks"),
    ISSUES("/api/v1/issues");

    private final String path;
}
