package com.manolito.dashflow.service.dw;

import com.manolito.dashflow.loader.TasksDataWarehouseLoader;
import com.manolito.dashflow.util.SparkUtils;
import lombok.RequiredArgsConstructor;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.functions;
import org.springframework.stereotype.Service;

import java.util.Arrays;

import static com.manolito.dashflow.enums.ProjectManagementTool.JIRA;

@Service
@RequiredArgsConstructor
public class JiraService {

    private final SparkSession spark;
    private final SparkUtils utils;
    private final TasksDataWarehouseLoader dataWarehouseLoader;

    private String mapNameField(String tableName) {
        return switch (tableName) {
            case "projects" -> "project_name";
            case "status" -> "status_name";
            case "users" -> "user_name";
            case "tags" -> "tag_name";
            default -> throw new IllegalArgumentException("Unsupported table for name field mapping: " + tableName);
        };
    }

    private Dataset<Row> fetchAndConvertToDataFrame(String endpoint, String tableName) {
        String jsonResponse = utils.fetchDataFromEndpointTrello(JIRA.getBaseUrl() + endpoint);
        Dataset<org.apache.spark.sql.Row> data = utils.fetchDataAsDataFrame(jsonResponse);

        data = data.withColumn("tool_id", functions.lit(3));

        String mappedNameColumn = mapNameField(tableName);
        if (Arrays.asList(data.columns()).contains("name")) {
            data = data.withColumnRenamed("name", mappedNameColumn);
        }

        return data;
    }
}
