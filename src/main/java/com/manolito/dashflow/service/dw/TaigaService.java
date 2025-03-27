package com.manolito.dashflow.service.dw;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.manolito.dashflow.dto.dw.TaigaAuthDto;
import com.manolito.dashflow.loader.TasksDataWarehouseLoader;
import com.manolito.dashflow.repository.dw.UserRepository;
import com.manolito.dashflow.transformer.TaigaTransformer;
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
import org.apache.spark.sql.types.LongType;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.manolito.dashflow.enums.ProjectManagementTool.TAIGA;
import static com.manolito.dashflow.enums.TaigaEndpoints.*;
import static org.apache.spark.sql.functions.*;

@Service
@RequiredArgsConstructor
public class TaigaService {

    private final SparkSession spark;
    private final SparkUtils utils;
    private final TasksDataWarehouseLoader dataWarehouseLoader;
    private final UserRepository userRepository;
    private static final String API_URL = "https://api.taiga.io/api/v1/auth";
    private static final String USER_ME_URL = "https://api.taiga.io/api/v1/users/me";
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private String authToken;
    private Integer userId;
    private String project = String.valueOf(1637322);

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
        return fetchAndConvertToDataFrame(PROJECTS.getPath() + "/" + project, "projects");
    }

    public Dataset<Row> handleUserStories() {
        return fetchAndConvertToDataFrame(USER_STORIES.getPath() + "?project=" + project, "stories");
    }

    public Dataset<Row> handleTasks() {
        return fetchAndConvertToDataFrame(TASKS.getPath() + "?project=" + project, "fact_tasks");
    }

    public Dataset<Row> handleIssues() {
        return fetchAndConvertToDataFrame(ISSUES.getPath(), "PLACEHOLDER");
    }

    public Dataset<Row> handleEpics() {
        return fetchAndConvertToDataFrame(TASKS.getPath() + "?project=" + project, "epics");
    }

    public Dataset<Row> handleRoles() {
        return fetchAndConvertToDataFrame(PROJECTS.getPath() + "/" + project, "roles");
    }

    private Dataset<Row> handleStatus() {
        return fetchAndConvertToDataFrame(TASKS.getPath() + "?project=" + project, "status");
    }

    private Dataset<Row> handleUser() {
        return fetchAndConvertToDataFrame(PROJECTS.getPath() + "/" + project, "users");
    }

    private Dataset<Row> handleTags() {
        return fetchAndConvertToDataFrame(TASKS.getPath() + "?project=" + project, "tags");
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
            saveUserToDatabase();

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

        data = data.withColumn("tool_id", functions.lit(1));

        String mappedNameColumn = mapNameField(tableName);
        if (Arrays.asList(data.columns()).contains("name")) {
            data = data.withColumnRenamed("name", mappedNameColumn);
        }

        return data;
    }

    private void saveUserToDatabase() {
        if (authToken == null) {
            throw new IllegalStateException("Token not acquired");
        }

        String jsonResponse = utils.fetchDataFromEndpoint(USER_ME_URL, authToken);
        Dataset<Row> rawUsers = utils.fetchDataAsDataFrame(jsonResponse);

        TaigaTransformer transformer = new TaigaTransformer(spark.emptyDataFrame());
        Dataset<Row> transformedUsers = transformer.transformUsers(rawUsers);

        String originalId = extractOriginalIdFromDataset(transformedUsers);
        if (originalId == null) {
            return;
        }

        String projectsResponse = utils.fetchDataFromEndpoint(TAIGA.getBaseUrl() + PROJECTS.getPath() + "?member=" + originalId, authToken);
        Dataset<Row> projectsData = utils.fetchDataAsDataFrame(projectsResponse);

        if (projectsData.isEmpty()) {
            throw new IllegalStateException("Project not found.");
        }

        Row Project = projectsData.select("id").head();
        long projectId = Project.getLong(0);
        project = String.valueOf(projectId);
    }

    private String extractOriginalIdFromDataset(Dataset<Row> transformedUsers) {
        if (transformedUsers.isEmpty()) {
            System.out.println("No user data to save.");
            return null;
        }

        Row firstRow = transformedUsers.select("original_id").head();
        if (firstRow.schema().apply("original_id").dataType() instanceof LongType) {
            return String.valueOf(firstRow.getLong(0));
        } else {
            return firstRow.getString(0);
        }
    }

    private Map<Long, Long> mapFromLongList(List<Long[]> list) {
        return list.stream()
                .collect(Collectors.toMap(
                        pair -> pair[1],
                        pair -> pair[0]
                ));
    };
    
    public Dataset<Row> saveUserRoleToDatabase() {
        final long projectId = 1637322L;

        try {
            Dataset<Row> userRolePairs = handleProjects()
                    .withColumn("member", explode(col("members")))
                    .select(
                            col("member.id").cast("long").as("user_original_id"),
                            col("member.role").cast("long").as("role_original_id")
                    );

            Dataset<Row> roles = dataWarehouseLoader.loadDimensionWithoutTool("roles", "taiga")
                    .select(
                            col("role_id").cast("long").as("role_id"),
                            col("original_id").cast("long").as("role_original_id")
                    );

            Dataset<Row> users = dataWarehouseLoader.loadDimensionWithoutTool("users", "taiga")
                    .select(
                            col("user_id").cast("long").as("user_id"),
                            col("original_id").cast("long").as("user_original_id")
                    );

            return userRolePairs
                    .join(users, "user_original_id", "inner")
                    .join(roles, "role_original_id", "inner")
                    .select("user_id", "role_id");

        } catch (Exception e) {
            throw new RuntimeException("Failed to save user roles", e);
        }
    }

    public Dataset<Row> saveTaskTagToDatabase() {
        try {
            Dataset<Row> taskTagPairs = handleTasks()
                    .withColumn("tag", explode(col("tags")))
                    .select(
                            col("id").cast("long").as("task_original_id"),
                            col("tag").getItem(0).as("tag_name")
                    );

            Dataset<Row> tasks = dataWarehouseLoader.loadDimensionWithoutIsCurrent("fact_tasks", "taiga")
                    .select(
                            col("task_id").cast("long").as("task_id"),
                            col("original_id").cast("long").as("task_original_id")
                    );

            Dataset<Row> tags = dataWarehouseLoader.loadDimensionWithoutIsCurrent("tags", "taiga")
                    .select(
                            col("tag_id").cast("long").as("tag_id"),
                            col("tag_name").as("tag_name")
                    );

            return taskTagPairs
                    .join(tasks, "task_original_id", "inner")
                    .join(tags, "tag_name", "inner")
                    .select("task_id", "tag_id")
                    .orderBy("task_id", "tag_id");

        } catch (Exception e) {
            throw new RuntimeException("Failed to save task tags", e);
        }
    }

    public static Dataset<Row> updateStatusProjectId(Dataset<Row> statusDF, Dataset<Row> projectsDF) {
        Dataset<Row> joined = statusDF
                .join(projectsDF, statusDF.col("project_id").equalTo(projectsDF.col("original_id")));
        return joined.select(
                statusDF.col("original_id"),
                statusDF.col("status_name"),
                projectsDF.col("project_id").alias("project_id")
        );
    }
    
    public static Dataset<Row> updateStoryProjectAndEpicIds(Dataset<Row> storiesDF,
                                                            Dataset<Row> projectsDF,
                                                            Dataset<Row> epicsDF) {
        Dataset<Row> joinedWithProjects = storiesDF
                .join(projectsDF,
                        storiesDF.col("project_id").equalTo(projectsDF.col("original_id")));

        Dataset<Row> joinedWithEpics = joinedWithProjects
                .join(epicsDF,
                        joinedWithProjects.col("epic_id").equalTo(epicsDF.col("original_id")),
                        "left");

        return joinedWithEpics.select(
                storiesDF.col("original_id"),
                projectsDF.col("project_id"),
                storiesDF.col("epic_id").alias("epic_id"),
                storiesDF.col("story_name"),
                storiesDF.col("is_finished")
        );
    }

    public static Dataset<Row> updateFactTask(Dataset<Row> tasksDF,
                                              Dataset<Row> statusDF,
                                              Dataset<Row> userDF,
                                              Dataset<Row> storiesDF,
                                              Dataset<Row> datesDF) {

        Dataset<Row> joinedWithStatus = tasksDF
                .join(statusDF,
                        tasksDF.col("status_id").equalTo(statusDF.col("original_id")));

        Dataset<Row> joinedWithUser = joinedWithStatus
                .join(userDF,
                        joinedWithStatus.col("user_id").equalTo(userDF.col("original_id")));

        Dataset<Row> joinedWithStories = joinedWithUser
                .join(storiesDF,
                        joinedWithUser.col("story_id").equalTo(storiesDF.col("original_id")));

        Dataset<Row> withCreatedDate = joinedWithStories
                .join(datesDF,
                        joinedWithStories.col("created_at").cast("date").equalTo(datesDF.col("date_date")),
                        "left")
                .withColumnRenamed("date_id", "created_date_id")
                .drop("date_date", "month", "year", "quarter", "day_of_week",
                        "day_of_month", "day_of_year", "is_weekend");

        Dataset<Row> withCompletedDate = withCreatedDate
                .join(datesDF,
                        withCreatedDate.col("completed_at").cast("date").equalTo(datesDF.col("date_date")),
                        "left")
                .withColumnRenamed("date_id", "completed_date_id")
                .drop("date_date", "month", "year", "quarter", "day_of_week",
                        "day_of_month", "day_of_year", "is_weekend");

        Dataset<Row> withDueDate = withCompletedDate
                .join(datesDF,
                        withCompletedDate.col("due_date").cast("date").equalTo(datesDF.col("date_date")),
                        "left")
                .withColumnRenamed("date_id", "due_date_id")
                .drop("date_date", "month", "year", "quarter", "day_of_week",
                        "day_of_month", "day_of_year", "is_weekend");

        return withDueDate.select(
                tasksDF.col("original_id"),
                statusDF.col("status_id"),
                userDF.col("user_id").as("assignee_id"),
                tasksDF.col("tool_id"),
                storiesDF.col("story_id"),
                col("created_date_id").as("created_at"),
                col("completed_date_id").as("completed_at"),
                col("due_date_id").as("due_date"),
                tasksDF.col("task_name"),
                tasksDF.col("is_blocked"),
                tasksDF.col("is_storyless")
        );
    }
    //Remove post construct annotation after login is done
    @PostConstruct
    public void taigaEtl() {
        authenticateTaiga("gabguska", "aluno123");
        TaigaTransformer transformer = new TaigaTransformer(spark.emptyDataFrame());
        Dataset<Row> roles = transformer.transformRoles(handleRoles());
        Dataset<Row> users = transformer.transformedUserProjects(handleUser());
        Dataset<Row> projects = transformer.transformProjects(handleProjects());
        Dataset<Row> stories = transformer.transformUserStories(handleUserStories());
        stories = updateStoryProjectAndEpicIds(stories, dataWarehouseLoader.loadDimensionWithoutTool("projects", "taiga"),
                dataWarehouseLoader.loadDimensionWithoutTool("epics", "taiga")
        );
        Dataset<Row> tags = transformer.transformTags(handleTags());
        dataWarehouseLoader.save(roles,"roles");
        dataWarehouseLoader.save(users, "users");
        Dataset<Row> userRole = saveUserRoleToDatabase();
        dataWarehouseLoader.save(userRole,"user_role");
        dataWarehouseLoader.save(projects, "projects");
        Dataset<Row> status = transformer.transformStatus(handleStatus());
        status = updateStatusProjectId(status, dataWarehouseLoader.loadDimensionWithoutTool("projects","taiga"));
        dataWarehouseLoader.save(status, "status");
        dataWarehouseLoader.save(stories, "stories");
        dataWarehouseLoader.save(tags, "tags");
        Dataset<Row> tasks = transformer.transformTasks(handleTasks());
        tasks = updateFactTask(tasks,
                dataWarehouseLoader.loadDimension("status"),
                dataWarehouseLoader.loadDimension("users"),
                dataWarehouseLoader.loadDimension("stories"),
                dataWarehouseLoader.loadDimensionWithoutIsCurrent("dates", "taiga"));
        dataWarehouseLoader.save(tasks, "fact_tasks");
        Dataset<Row> taskTag = saveTaskTagToDatabase();
        dataWarehouseLoader.save(taskTag,"task_tag");
    }
}
