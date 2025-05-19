package com.manolito.dashflow.service.dw;

import com.manolito.dashflow.config.JiraConfig;
import com.manolito.dashflow.dto.dw.JiraAuthDto;
import com.manolito.dashflow.loader.TasksDataWarehouseLoader;
import com.manolito.dashflow.util.SparkUtils;
import lombok.RequiredArgsConstructor;
import org.apache.spark.sql.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.manolito.dashflow.enums.JiraEndpoints.*;
import static com.manolito.dashflow.enums.ProjectManagementTool.JIRA;

@Service
@RequiredArgsConstructor
public class JiraService {

    private final SparkSession spark;
    private final SparkUtils utils;
    private final TasksDataWarehouseLoader dataWarehouseLoader;
    private final JiraConfig jiraConfig;
    private List<String> projectKeys;
    private JiraAuthDto buildAuthDto() {
        return JiraAuthDto.builder()
                .email(jiraConfig.getEmail())
                .apiToken(jiraConfig.getToken())
                .build();
    }


    private String mapNameField(String tableName) {
        return switch (tableName) {
            case "projects" -> "project_name";
            case "status" -> "status_name";
            case "users" -> "user_name";
            case "tags" -> "tag_name";
            case "tasks" -> "fact_task";
            default -> throw new IllegalArgumentException("Unsupported table for name field mapping: " + tableName);
        };
    }

    public List<Dataset<Row>> handleUsers() {
        List<Dataset<Row>> usersData = new ArrayList<>();
        String endpoint = USERS.getPath();
        Dataset<Row> userDF = fetchAndConvertToDataFrame(endpoint, "users", buildAuthDto());
        usersData.add(userDF);
        return usersData;
    }

    public void getProjectsWhereUserIsMember() {
        String jsonResponse = utils.fetchDataFromJira(JIRA.getBaseUrl() + PROJECT.getPath(), buildAuthDto());
        Dataset<Row> df = utils.fetchDataAsDataFrame(jsonResponse);

        projectKeys = df.select("key")
                .as(Encoders.STRING())
                .collectAsList();
    }

    public List<Dataset<Row>> handleProjects() {
        List<Dataset<Row>> projectsData = new ArrayList<>();
        String endpoint = PROJECT.getPath();
        Dataset<Row> projectsDF = fetchAndConvertToDataFrame(endpoint, "projects", buildAuthDto());
        projectsData.add(projectsDF);
        return projectsData;
    }

    public List<Dataset<Row>> handleStatus() {
        List<Dataset<Row>> statusData = new ArrayList<>();
        String endpoint = STATUS.getPath();
        Dataset<Row> statusDF = fetchAndConvertToDataFrame(endpoint, "status", buildAuthDto());
        statusData.add(statusDF);
        return statusData;
    }

    public List<Dataset<Row>> handleTasks() {
        List<Dataset<Row>> tasksData = new ArrayList<>();

        for (String projectKey : projectKeys) {
            String endpoint = TASKS.getPath().replace("{projectKey}", projectKey);
            Dataset<Row> taskDF = fetchAndConvertToDataFrame(endpoint, "tasks", buildAuthDto());
            tasksData.add(taskDF);
        }

        return tasksData;
    }

    public List<Dataset<Row>> handleTags() {
        List<Dataset<Row>> tagsData = new ArrayList<>();
        for (String projectKey : projectKeys) {
            String endpoint = TASKS.getPath().replace("{projectKey}", projectKey);
            Dataset<Row> tagsDF = fetchAndConvertToDataFrame(endpoint, "tags", buildAuthDto());
            tagsData.add(tagsDF);
        }
        return tagsData;
    }

    private Dataset<Row> fetchAndConvertToDataFrame(String endpoint, String tableName, JiraAuthDto jiraAuthDto) {
        String jsonResponse = utils.fetchDataFromJira(JIRA.getBaseUrl() + endpoint, jiraAuthDto);
        Dataset<org.apache.spark.sql.Row> data = utils.fetchDataAsDataFrame(jsonResponse);

        data = data.withColumn("tool_id", functions.lit(3));

        String mappedNameColumn = mapNameField(tableName);
        if (Arrays.asList(data.columns()).contains("name")) {
            data = data.withColumnRenamed("name", mappedNameColumn);
        }

        return data;
    }
}
