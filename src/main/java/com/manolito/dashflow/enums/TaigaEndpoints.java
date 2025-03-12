package com.manolito.dashflow.enums;

import com.manolito.dashflow.service.EndpointService;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TaigaEndpoints implements EndpointService {

    PROJECTS("/api/v1/projects") {
        @Override
        public void handleRequest(String requestUrl) {
        }
    },

    USER_STORIES("/api/v1/userstories") {
        @Override
        public void handleRequest(String requestUrl) {
        }
    },

    TASKS("/api/v1/tasks") {
        @Override
        public void handleRequest(String requestUrl) {
        }
    },

    ISSUES("/api/v1/issues") {
        @Override
        public void handleRequest(String requestUrl) {
        }
    },

    USER_STORY_STATUSES("/api/v1/userstory-statuses") {
        @Override
        public void handleRequest(String requestUrl) {
        }
    },

    EPICS("/api/v1/epics") {
        @Override
        public void handleRequest(String requestUrl) {
        }
    },

    ROLES("/api/v1/roles") {
        @Override
        public void handleRequest(String requestUrl) {
        }
    },

    PROJECT_MEMBERS("/api/v1/projects/{project_id}/members") {
        @Override
        public void handleRequest(String requestUrl) {
        }
    };

    private final String path;
}