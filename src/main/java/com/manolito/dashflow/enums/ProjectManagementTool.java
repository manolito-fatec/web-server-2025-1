package com.manolito.dashflow.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ProjectManagementTool {

    TAIGA("https://api.taiga.io", TaigaEndpoints.class);

    private final String baseUrl;
    private final Class<? extends Enum<?>> endpointsClass;

    public String getFullUrl(Enum<?> endpoint) {
        if (endpoint instanceof TaigaEndpoints taigaEndpoint) {
            return this.baseUrl + taigaEndpoint.getPath();
        }
        throw new IllegalArgumentException("Endpoint não suportado: " + endpoint);
    }
}
