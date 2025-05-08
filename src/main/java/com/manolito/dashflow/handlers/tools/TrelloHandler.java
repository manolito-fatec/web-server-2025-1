package com.manolito.dashflow.handlers.tools;

import com.manolito.dashflow.enums.TrelloEndpoints;
import com.manolito.dashflow.service.dw.TrelloService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TrelloHandler implements ToolHandler{

    private final TrelloService trelloService;

    @Override
    public void handleRequest(String endpoint) {
        TrelloEndpoints object = TrelloEndpoints.identifyObject(endpoint);

        if (object == null) {
            throw new RuntimeException("Error on Identifying the Trello Object");
        }

        switch (object) {
            case BOARDS:
                trelloService.handleProjectsBoards();
                break;
            case CARDS:
                trelloService.handleCards();
                break;
            case USERS:
                trelloService.handleUsers();
            case LISTS:
                trelloService.handleLists();
            case TAGS:
                trelloService.handleTags();
            default:
                throw new RuntimeException("Unsupported Taiga Object");
        }
    }
}
