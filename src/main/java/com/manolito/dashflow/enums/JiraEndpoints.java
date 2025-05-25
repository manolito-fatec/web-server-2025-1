package com.manolito.dashflow.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum JiraEndpoints {

    USERS("/users"),
    PROJECT("/project"),
    STATUS("/status"),
    TASKS("/search?jql=project={projectKey}");

   private final String path;

    public static JiraEndpoints identifyObject(String endpointPath) {
        for (JiraEndpoints endpoint : values()) {
            if (endpointPath.startsWith(endpoint.getPath())) {
                return endpoint;
            }
        }
        return null;
    }
}
