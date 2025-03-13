package com.manolito.dashflow.service.dw;

import com.manolito.dashflow.enums.ProjectManagementTool;
import com.manolito.dashflow.enums.TaigaEndpoints;
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

    private Dataset<Row> fetchDataAsDataFrame(String url) {
        String jsonResponse = utils.fetchDataFromEndpoint(url);
        return spark.read().json(spark.createDataset(List.of(jsonResponse), Encoders.STRING()));
    }

}
