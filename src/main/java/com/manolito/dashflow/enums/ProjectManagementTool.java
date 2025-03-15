package com.manolito.dashflow.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ProjectManagementTool {

    TAIGA("https://api.taiga.io"),
    TRELLO("https://api.trello.com"),
    CLICKUP("https://api.clickup.com/api");

    private final String baseUrl;

    public String getFullUrl(Enum<?> endpoint) {
        if (endpoint instanceof TaigaEndpoints taigaEndpoint) {
            return this.baseUrl + taigaEndpoint.getPath();
        }
        throw new IllegalArgumentException("Endpoint não suportado: " + endpoint);
    }

    public static ProjectManagementTool identifyTool(String requestUrl) {
        for (ProjectManagementTool tool : values()) {
            if (requestUrl.contains(tool.getBaseUrl())) {
                return tool;
            }
        }
        return null;
    }
}
