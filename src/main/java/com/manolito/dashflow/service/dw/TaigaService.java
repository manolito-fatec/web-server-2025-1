package com.manolito.dashflow.service.dw;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.manolito.dashflow.dto.dw.TaigaAuthDto;
import com.manolito.dashflow.dto.dw.UserDto;
import com.manolito.dashflow.loader.TasksDataWarehouseLoader;
import com.manolito.dashflow.util.SparkUtils;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import org.apache.spark.sql.functions;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import static com.manolito.dashflow.enums.ProjectManagementTool.TAIGA;
import static com.manolito.dashflow.enums.TaigaEndpoints.*;

@Service
@RequiredArgsConstructor
public class TaigaService {

    private final SparkSession spark;
    private final SparkUtils utils;
    private final TasksDataWarehouseLoader dataWarehouseLoader;
    private static final String API_URL = "https://api.taiga.io/api/v1/auth";
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private String authToken;
    private Integer userId;

    /**
     * Maps the "name" field to the appropriate column name based on the target table.
     *
     * @param tableName The name of the target table.
     * @return The mapped column name for the "name" field.
     */
    private String mapNameField(String tableName) {
        return switch (tableName) {
            case "projects" -> "project_name";
            case "issues" -> "issue_name";
            case "status" -> "status_name";
            case "stories" -> "story_name";
            case "epics" -> "epic_name";
            case "roles" -> "role_name";
            case "users" -> "user_name";
            case "tags" -> "tag_name";
            case "fact_tasks" -> "task_name";
            default -> throw new IllegalArgumentException("Unsupported table for name field mapping: " + tableName);
        };
    }


    public Dataset<Row> handleProjects() {
        return fetchAndConvertToDataFrame(PROJECTS.getPath(), "projects");
    }

    public Dataset<Row> handleUserStories() {
        return fetchAndConvertToDataFrame(USER_STORIES.getPath(), "stories");
    }

    public Dataset<Row> handleTasks() {
        return fetchAndConvertToDataFrame(TASKS.getPath(), "fact_tasks");
    }

    public Dataset<Row> handleIssues() {
        return fetchAndConvertToDataFrame(ISSUES.getPath(), "PLACEHOLDER");
    }

    public Dataset<Row> handleUsersStoriesStatus() {
        return fetchAndConvertToDataFrame(USER_STORY_STATUSES.getPath(), "PLACEHOLDER");
    }

    public Dataset<Row> handleEpics() {
        return fetchAndConvertToDataFrame(EPICS.getPath(), "epics");
    }

    public Dataset<Row> handleRoles() {
        return fetchAndConvertToDataFrame(ROLES.getPath(), "roles");
    }

    public Dataset<Row> handleProjectMembers() {
        return fetchAndConvertToDataFrame(PROJECT_MEMBERS.getPath(), "PLACEHOLDER");
    }

    public void authenticateTaiga(String username, String password) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(API_URL);

            post.addHeader("Accept", "application/json");
            post.addHeader("Content-Type", "application/json");

            TaigaAuthDto authRequest = TaigaAuthDto.builder()
                    .username(username)
                    .password(password)
                    .type("normal")
                    .build();

            String jsonBody = objectMapper.writeValueAsString(authRequest);

            StringEntity entity = new StringEntity(jsonBody, StandardCharsets.UTF_8);
            post.setEntity(entity);

            HttpResponse response = httpClient.execute(post);

            if (response.getStatusLine().getStatusCode() != 200) {
                throw new RuntimeException("Falha na autenticação: " + response.getStatusLine().getStatusCode());
            }

            String responseString = EntityUtils.toString(response.getEntity());
            JsonNode jsonNode = objectMapper.readTree(responseString);

            authToken = jsonNode.get("auth_token").asText();

        } catch (Exception e) {
            throw new RuntimeException("Erro ao autenticar no Taiga", e);
        }
    }

    /**
     * Fetches data from the specified endpoint and converts it into a DataFrame.
     *
     * @param endpoint The endpoint path (e.g., PROJECTS.getPath()).
     * @return A DataFrame containing the fetched data.
     */
    private Dataset<Row> fetchAndConvertToDataFrame(String endpoint, String tableName) {
        authenticateTaiga("gabguska", "aluno123");
        String jsonResponse = utils.fetchDataFromEndpoint(TAIGA.getBaseUrl() + endpoint, authToken);
        Dataset<Row> data = utils.fetchDataAsDataFrame(jsonResponse);

        // Add tool_id = 1 to the DataFrame
        data = data.withColumn("tool_id", functions.lit(1));

        // Map the "name" field to the appropriate column name
        String mappedNameColumn = mapNameField(tableName);
        if (Arrays.asList(data.columns()).contains("name")) {
            data = data.withColumnRenamed("name", mappedNameColumn);
        }

        return data;
    }

    private void saveUserToDatabase(Integer userId, String username) {
        UserDto userDto = new UserDto(userId, username);

        Dataset<Row> userData = spark.createDataFrame(
                List.of(userDto),
                UserDto.class
        );

        dataWarehouseLoader.save(userData, "users");
        System.out.println("Usuário salvo no banco de dados: " + username);
    }

}
