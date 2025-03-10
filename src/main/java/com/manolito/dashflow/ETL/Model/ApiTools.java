package com.manolito.dashflow.ETL.Model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ApiTools {

    TAIGA("https://api.taiga.io", TaigaEndpoints.class),;

    private final String baseUrl;
    private final Class<? extends Enum<?>> endpointsClass;

    public String getFullUrl(Enum<?> endpoint) {
        try {
            String path = (String) endpoint.getClass().getMethod("getPath").invoke(endpoint);
            return this.baseUrl + path;
        } catch (Exception e) {
            throw new RuntimeException("Erro ao construir a URL", e);
        }
    }
}
