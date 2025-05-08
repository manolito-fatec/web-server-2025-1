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
import java.util.List;

import static com.manolito.dashflow.enums.ProjectManagementTool.TRELLO;

@Service
@RequiredArgsConstructor
public class TrelloService {

    private final SparkSession spark;
    private final SparkUtils utils;
    private final TasksDataWarehouseLoader dataWarehouseLoader;

    /**
     * Maps the "name" field to the appropriate column name based on the target table.
     *
     * @param tableName The name of the target table.
     * @return The mapped column name for the "name" field.
     */
    private String mapNameField(String tableName) {
        return switch (tableName) {
            case "projects" -> "project_name";
            case "status" -> "status_name";
            case "users" -> "user_name";
            case "tags" -> "tag_name";
            case "cards" -> "task_name";
            default -> throw new IllegalArgumentException("Unsupported table for name field mapping: " + tableName);
        };
    }

    private Dataset<Row> fetchAndConvertToDataFrame(String endpoint,String tableName) {
        String jsonResponse = utils.fetchDataFromEndpointTrello(TRELLO.getBaseUrl() + endpoint);
        Dataset<org.apache.spark.sql.Row> data = utils.fetchDataAsDataFrame(jsonResponse);

        data = data.withColumn("tool_id", functions.lit(2));

        String mappedNameColumn = mapNameField(tableName);
        if (Arrays.asList(data.columns()).contains("name")) {
            data = data.withColumnRenamed("name", mappedNameColumn);
        }

        return data;
    }

    public List<Dataset<Row>> handleBoards() {
        //future implementation
        return null;
    }

    public List<Dataset<Row>> handleList() {
        //future implementation
        return null;
    }

    public List<Dataset<Row>> handleCards() {
        //future implementation
        return null;
    }

}
