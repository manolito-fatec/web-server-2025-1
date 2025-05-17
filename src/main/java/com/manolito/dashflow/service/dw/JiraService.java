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
