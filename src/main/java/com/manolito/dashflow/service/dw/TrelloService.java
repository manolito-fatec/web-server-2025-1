package com.manolito.dashflow.service.dw;

import com.manolito.dashflow.loader.TasksDataWarehouseLoader;
import com.manolito.dashflow.util.SparkUtils;
import lombok.RequiredArgsConstructor;
import org.apache.spark.sql.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.manolito.dashflow.enums.ProjectManagementTool.TRELLO;
import static com.manolito.dashflow.enums.TrelloEndpoints.*;

@Service
@RequiredArgsConstructor
public class TrelloService {

    private final SparkSession spark;
    private final SparkUtils utils;
    private final TasksDataWarehouseLoader dataWarehouseLoader;
    private List<String> projectIds;
    private List<String> boardsIds;
    private String keyToken;

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

    /**
     * Fetches data from the specified endpoint and converts it into a DataFrame.
     *
     * @param endpoint The endpoint path (e.g., PROJECTS.getPath()).
     * @return A DataFrame containing the fetched data.
     */
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

    /**
     * Fetches project and board data from the Trello API endpoint and extracts unique project and board IDs.
     * The method retrieves all boards and then collects distinct organization IDs (projects) and board IDs.
     * The results are stored in class-level variables projectIds and boardsIds.
     */
    public void handleProjectsBoards() {
        String jsonResponse = utils.fetchDataFromEndpointTrello(TRELLO.getBaseUrl() + BOARDS.getPath() + keyToken);
        Dataset<Row> df = utils.fetchDataAsDataFrame(jsonResponse);

        projectIds = df.select("idOrganization")
                .dropDuplicates("idOrganization")
                .as(Encoders.STRING())
                .collectAsList();

        boardsIds = df.select("id")
                .dropDuplicates("id")
                .as(Encoders.STRING())
                .collectAsList();
    }

    /**
     * Fetches project data from the Trello API for each project ID.
     * Retrieves all projects for each project ID stored in projectIds and converts them to DataFrames.
     *
     * @return List of Datasets containing project data, one Dataset per project ID
     */
    public List<Dataset<Row>> handleProjects() {
        List<Dataset<Row>> projectsData = new ArrayList<>();

        for (String projectId : projectIds) {
            String endpoint = PROJECTS.getPath().replace("{idOrganization}", projectId) + keyToken;
            Dataset<Row> projectsDF = fetchAndConvertToDataFrame(endpoint, "projects");
            projectsData.add(projectsDF);
            projectsDF.show();
        }
        return projectsData;
    }

    /**
     * Fetches list data (statuses) from the Trello API for each board.
     * Retrieves all lists for each board ID stored in boardsIds and converts them to DataFrames.
     *
     * @return List of Datasets containing list/status data, one Dataset per board ID
     */
    public List<Dataset<Row>> handleLists() {
        List<Dataset<Row>> statusData = new ArrayList<>();

        for (String boardId : boardsIds) {
            String endpoint = LISTS.getPath().replace("{boardId}", boardId) + keyToken;
            Dataset<Row> statusDF = fetchAndConvertToDataFrame(endpoint, "status");
            statusData.add(statusDF);
        }

        return statusData;
    }

    /**
     * Fetches user data from the Trello API for each project (organization).
     * Retrieves all members for each project ID stored in projectIds and converts them to DataFrames.
     *
     * @return List of Datasets containing user data, one Dataset per project ID
     */
    public List<Dataset<Row>> handleUsers() {
        List<Dataset<Row>> usersData = new ArrayList<>();

        for (String projectId : projectIds) {
            String endpoint = USERS.getPath().replace("{IdOrganization}", projectId) + keyToken;
            Dataset<Row> usersDF = fetchAndConvertToDataFrame(endpoint, "users");
            usersData.add(usersDF);
        }

        return usersData;
    }

    /**
     * Fetches tag/label data from the Trello API for each board.
     * Retrieves all labels for each board ID stored in boardsIds and converts them to DataFrames.
     *
     * @return List of Datasets containing tag/label data, one Dataset per board ID
     */
    public List<Dataset<Row>> handleTags() {
        List<Dataset<Row>> tagsData = new ArrayList<>();
        for (String boardId : boardsIds) {
            String endpoint = TAGS.getPath().replace("{boardId}", boardId) + keyToken;
            Dataset<Row> tagsDF = fetchAndConvertToDataFrame(endpoint, "tags");
            tagsData.add(tagsDF);
        }
        return tagsData;
    }

    /**
     * Fetches card data from the Trello API for each board.
     * Retrieves all cards for each board ID stored in boardsIds and converts them to DataFrames.
     *
     * @return List of Datasets containing card data, one Dataset per board ID
     */
    public List<Dataset<Row>> handleCards() {
        List<Dataset<Row>> cardsData = new ArrayList<>();

        for (String boardId: boardsIds) {
            String endpoint = CARDS.getPath().replace("{boardId}", boardId) + keyToken;
            Dataset<Row> cardsDF = fetchAndConvertToDataFrame(endpoint, "cards");
            cardsData.add(cardsDF);
        }

        return cardsData;
    }

}
