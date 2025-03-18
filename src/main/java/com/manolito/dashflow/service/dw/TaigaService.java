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
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
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

    public Dataset<Row> handleProjects() {
        String memberFilter = "?member=" + userId;
        String projectsUrl = TAIGA.getBaseUrl() + PROJECTS.getPath() + memberFilter;
        return fetchDataAsDataFrame(projectsUrl, authToken);
    }

    public Dataset<Row> handleUserStories() {
        return fetchDataAsDataFrame(TAIGA.getBaseUrl() + USER_STORIES.getPath(), authToken);
    }

    public Dataset<Row> handleTasks() {
        return fetchDataAsDataFrame(TAIGA.getBaseUrl() + TASKS.getPath(), authToken);
    }

    public Dataset<Row> handleIssues() {
        return fetchDataAsDataFrame(TAIGA.getBaseUrl() + ISSUES.getPath(), authToken);
    }

    public Dataset<Row> handleUsersStoriesStatus() {
        return fetchDataAsDataFrame(TAIGA.getBaseUrl() + USER_STORY_STATUSES.getPath(), authToken);
    }

    public Dataset<Row> handleEpics() {
        return fetchDataAsDataFrame(TAIGA.getBaseUrl() + EPICS.getPath(), authToken);
    }

    public Dataset<Row> handleRoles() {
        return fetchDataAsDataFrame(TAIGA.getBaseUrl() + ROLES.getPath(), authToken);
    }

    public Dataset<Row> handleProjectMembers() {
        return fetchDataAsDataFrame(TAIGA.getBaseUrl() + PROJECT_MEMBERS.getPath(), authToken);
    }

    private Dataset<Row> fetchDataAsDataFrame(String url, String authToken) {
        String jsonResponse = utils.fetchDataFromEndpoint(url, authToken);
        return spark.read().json(spark.createDataset(List.of(jsonResponse), Encoders.STRING()));
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
            userId = jsonNode.get("id").asInt();

        } catch (Exception e) {
            throw new RuntimeException("Erro ao autenticar no Taiga", e);
        }
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
