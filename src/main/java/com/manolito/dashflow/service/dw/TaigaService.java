package com.manolito.dashflow.service.dw;

import com.manolito.dashflow.util.SparkUtils;
import lombok.RequiredArgsConstructor;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import org.springframework.stereotype.Service;

import java.util.List;

import static com.manolito.dashflow.enums.ProjectManagementTool.TAIGA;
import static com.manolito.dashflow.enums.TaigaEndpoints.*;

@Service
@RequiredArgsConstructor
public class TaigaService {

    private final SparkSession spark;
    private final SparkUtils utils;

    public Dataset<Row> handleProjects() {
        return fetchDataAsDataFrame(TAIGA.name() + PROJECTS.name());
    }

    public Dataset<Row> handleUserStories() {
        return fetchDataAsDataFrame(TAIGA.name() + USER_STORIES.name());
    }

    public Dataset<Row> handleTasks() {
        return fetchDataAsDataFrame(TAIGA.name() + TASKS.name());
    }


    public Dataset<Row> handleIssues() {
        return fetchDataAsDataFrame(TAIGA.name() + ISSUES.name());
    }

    public Dataset<Row> handleUsersStoriesStatus() {
        return fetchDataAsDataFrame(TAIGA.name() + USER_STORY_STATUSES.name());
    }

    public Dataset<Row> handleEpics() {
        return fetchDataAsDataFrame(TAIGA.name() + EPICS.name());
    }

    public Dataset<Row> handleRoles() {
        return fetchDataAsDataFrame(TAIGA.name() + ROLES.name());
    }

    public Dataset<Row> handleProjectMembers() {
        return fetchDataAsDataFrame(TAIGA.name() + PROJECT_MEMBERS.name());
    }

    private Dataset<Row> fetchDataAsDataFrame(String url) {
        String jsonResponse = utils.fetchDataFromEndpoint(url);
        return spark.read().json(spark.createDataset(List.of(jsonResponse), Encoders.STRING()));
    }

}
