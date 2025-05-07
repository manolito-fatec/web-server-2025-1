package com.manolito.dashflow.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TrelloEndpoints {

    BOARDS("/members/me/boards?"),
    LIST("/boards/{boardId}/lists?"),
    CARDS("/boards/{boardId}/cards?");

    private final String path;

    public static TrelloEndpoints identifyObject(String endpointPath) {
        for (TrelloEndpoints endpoint : values()) {
            if (endpointPath.startsWith(endpoint.getPath())) {
                return endpoint;
            }
        }
        return null;
    }
}
