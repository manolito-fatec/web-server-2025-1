package com.manolito.dashflow.service.dw;

import com.manolito.dashflow.loader.TasksDataWarehouseLoader;
import com.manolito.dashflow.util.SparkUtils;
import lombok.RequiredArgsConstructor;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class TrelloService {

    private final SparkSession spark;
    private final SparkUtils utils;
    private final TasksDataWarehouseLoader dataWarehouseLoader;

    private String mapNameField(String tableName) {
        return switch (tableName) {
            case "boards" -> "project_name";
            case "lists" -> "story_name";
            case "cards" -> "task_name";
            default -> throw new IllegalArgumentException("Unsupported table for name field mapping: " + tableName);
        };
    }

    public Dataset<Row> handleBoards() {
        //future implementation
        return null;
    }

    public Dataset<Row> handleList() {
        //future implementation
        return null;
    }

    public Dataset<Row> handleCards() {
        //future implementation
        return null;
    }


}
