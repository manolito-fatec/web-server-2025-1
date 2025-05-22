package com.manolito.dashflow.service.dw;

import com.manolito.dashflow.config.JiraConfig;
import com.manolito.dashflow.dto.dw.JiraAuthDto;
import com.manolito.dashflow.loader.TasksDataWarehouseLoader;
import com.manolito.dashflow.transformer.JiraTransformer;
import com.manolito.dashflow.util.SparkUtils;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
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

    private static final String ACCEPT_HEADER = "Accept";
    private static final String APPLICATION_JSON = "application/json";
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
            case "tasks" -> "fact_task";
            default -> throw new IllegalArgumentException("Unsupported table for name field mapping: " + tableName);
        };
    }

    /**
     * Fetches data from a Jira API endpoint using Basic Authentication.
     *
     * @param url The URL of the Jira API endpoint to fetch data from
     * @param authDto The authentication DTO containing email and API token
     * @return The response body as a JSON string
     * @throws RuntimeException if the HTTP request fails or returns non-200 status code
     *                         or if any other error occurs during the request
     */
    public String fetchDataFromJira(String url, JiraAuthDto authDto) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(url);

            request.addHeader("Authorization", getAuthHeader(authDto));
            request.addHeader(ACCEPT_HEADER, APPLICATION_JSON);

            HttpResponse response = httpClient.execute(request);

            if (response.getStatusLine().getStatusCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : " + response.getStatusLine().getStatusCode());
            }

            return EntityUtils.toString(response.getEntity());
        } catch (Exception e) {
            throw new RuntimeException("Error fetching data from Jira endpoint: " + url, e);
        }
    }

    /**
     * Generates the Basic Authentication header value for Jira API requests.
     *
     * @param authDto The authentication DTO containing email and API token
     * @return The Base64 encoded Basic Authentication header value in the format "Basic [encoded_credentials]"
     */
    private String getAuthHeader(JiraAuthDto authDto) {
        String credentials = authDto.getEmail() + ":" + authDto.getApiToken();
        String encodedCredentials = java.util.Base64.getEncoder().encodeToString(credentials.getBytes());
        return "Basic " + encodedCredentials;
    }

    /**
     * Fetches user data from the API endpoint and converts it into a DataFrame.
     * The user data is obtained from a single endpoint without project filtering.
     *
     * @return List containing a single Dataset with user data
     */
    public List<Dataset<Row>> handleUsers() {
        List<Dataset<Row>> usersData = new ArrayList<>();
        String endpoint = USERS.getPath();
        Dataset<Row> userDF = fetchAndConvertToDataFrame(endpoint, "users", buildAuthDto());
        usersData.add(userDF);
        return usersData;
    }

    /**
     * Retrieves project keys where the authenticated user is a member.
     * The project keys are stored in the class field projectKeys for later use.
     */
    public void getProjectsWhereUserIsMember() {
        String jsonResponse = fetchDataFromJira(JIRA.getBaseUrl() + PROJECT.getPath(), buildAuthDto());
        Dataset<Row> df = utils.fetchDataAsDataFrame(jsonResponse);

        projectKeys = df.select("key")
                .as(Encoders.STRING())
                .collectAsList();
    }

    /**
     * Fetches project data from the API endpoint and converts it into a DataFrame.
     * The project data is obtained from a single endpoint without additional filtering.
     *
     * @return List containing a single Dataset with project data
     */
    public List<Dataset<Row>> handleProjects() {
        List<Dataset<Row>> projectsData = new ArrayList<>();
        String endpoint = PROJECT.getPath();
        Dataset<Row> projectsDF = fetchAndConvertToDataFrame(endpoint, "projects", buildAuthDto());
        projectsData.add(projectsDF);
        return projectsData;
    }

    /**
     * Fetches status data from the API endpoint and converts it into a DataFrame.
     * The status data represents workflow states available in Jira.
     *
     * @return List containing a single Dataset with status data
     */
    public List<Dataset<Row>> handleStatus() {
        List<Dataset<Row>> statusData = new ArrayList<>();
        String endpoint = STATUS.getPath();
        Dataset<Row> statusDF = fetchAndConvertToDataFrame(endpoint, "status", buildAuthDto());
        statusData.add(statusDF);
        return statusData;
    }

    /**
     * Fetches task data from the API endpoint for each project key and converts it into DataFrames.
     * Tasks are filtered by project key in the API endpoint URL.
     *
     * @return List of Datasets containing task data, one Dataset per project key
     */
    public List<Dataset<Row>> handleTasks() {
        List<Dataset<Row>> tasksData = new ArrayList<>();

        for (String projectKey : projectKeys) {
            String endpoint = TASKS.getPath().replace("{projectKey}", projectKey);
            Dataset<Row> taskDF = fetchAndConvertToDataFrame(endpoint, "tasks", buildAuthDto());
            tasksData.add(taskDF);
        }

        return tasksData;
    }

    /**
     * Fetches tag data (labels) from the API endpoint for each project key and converts it into DataFrames.
     * Tags are obtained from the tasks endpoint as Jira doesn't have a dedicated tags endpoint.
     *
     * @return List of Datasets containing tag data, one Dataset per project key
     */
    public List<Dataset<Row>> handleTags() {
        List<Dataset<Row>> tagsData = new ArrayList<>();
        for (String projectKey : projectKeys) {
            String endpoint = TASKS.getPath().replace("{projectKey}", projectKey);
            Dataset<Row> tagsDF = fetchAndConvertToDataFrame(endpoint, "tags", buildAuthDto());
            tagsData.add(tagsDF);
        }
        return tagsData;
    }

    /**
     * Fetches data from the specified Jira API endpoint and converts it into a formatted DataFrame.
     * Adds tool-specific columns and renames standard fields according to the table mapping.
     *
     * @param endpoint The API endpoint path
     * @param tableName The target table name for field mapping
     * @param jiraAuthDto Authentication DTO for Jira API
     * @return Formatted DataFrame with the fetched data
     */
    private Dataset<Row> fetchAndConvertToDataFrame(String endpoint, String tableName, JiraAuthDto jiraAuthDto) {
        String jsonResponse = fetchDataFromJira(JIRA.getBaseUrl() + endpoint, jiraAuthDto);
        Dataset<org.apache.spark.sql.Row> data = utils.fetchDataAsDataFrame(jsonResponse);

        data = data.withColumn("tool_id", functions.lit(3));

        String mappedNameColumn = mapNameField(tableName);
        if (Arrays.asList(data.columns()).contains("name")) {
            data = data.withColumnRenamed("name", mappedNameColumn);
        }

        return data;
    }

    private void processUsersData(JiraTransformer transformer) {
        List<Dataset<Row>> usersList = handleUsers();
        for (Dataset<Row> userDF : usersList) {
            Dataset<Row> transformedUsers = transformer.transformerUsers(userDF);
        }
    }

    private void processProjectsData(JiraTransformer transformer) {
        List<Dataset<Row>> projectsList = handleProjects();
        for (Dataset<Row> projectDF : projectsList) {
            Dataset<Row> transformedProjects = transformer.transformedProjects(projectDF);
        }
    }

    private void processStatusData(JiraTransformer transformer) {
        List<Dataset<Row>> statusList = handleStatus();
        for (Dataset<Row> statusDF : statusList) {
            Dataset<Row> transformedStatus = transformer.transformedStatus(statusDF);
        }
    }

    private void processTagsData(JiraTransformer transformer) {
        List<Dataset<Row>> tagsList = handleTags();
        for (Dataset<Row> tagsDF : tagsList) {
            Dataset<Row> transformedTags = transformer.transformedTags(tagsDF);
        }
    }
}
