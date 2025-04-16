package com.manolito.dashflow.service.dw;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.manolito.dashflow.dto.dw.TaigaAuthDto;
import com.manolito.dashflow.loader.TasksDataWarehouseLoader;
import com.manolito.dashflow.transformer.TaigaTransformer;
import com.manolito.dashflow.util.SparkUtils;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.spark.sql.*;
import org.apache.spark.sql.types.LongType;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
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
    private static final String API_URL = "https://api.taiga.io/api/v1/auth";
    private static final String USER_ME_URL = "https://api.taiga.io/api/v1/users/me";
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private String authToken;
    private List<Long> projectIds;

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

    /**
     * Fetches the data in the given endpoint and convert it into dataframe
     *
     * @return A ArrayList of Datasets from each endpoint
     */
    public List<Dataset<Row>> handleProjects() {
        List<Dataset<Row>> projectsData = new ArrayList<>();

        for (Long projectId : projectIds) {
            String endpoint = PROJECTS.getPath() + "/" + projectId;
            Dataset<Row> projectDF = fetchAndConvertToDataFrame(endpoint, "projects");
            projectsData.add(projectDF);
        }

        return projectsData;
    }

    /**
     * Fetches the data in the given endpoint and convert it into dataframe
     *
     * @return A ArrayList of Datasets from each endpoint
     */
    public List<Dataset<Row>> handleTasks() {
        List<Dataset<Row>> tasksData = new ArrayList<>();

        for (Long projectId : projectIds) {
            String endpoint = TASKS.getPath() + "?project=" + projectId;
            Dataset<Row> taskDF = fetchAndConvertToDataFrame(endpoint, "fact_tasks");
            tasksData.add(taskDF);
        }

        return tasksData;
    }


    /**
     * Fetches the data in the given endpoint and convert it into dataframe
     *
     * @return A ArrayList of Datasets from each endpoint
     */
    public List<Dataset<Row>> handleUserStories() {
        List<Dataset<Row>> userStoriesData = new ArrayList<>();

        for (Long projectId : projectIds) {
            String endpoint = USER_STORIES.getPath() + "?project=" + projectId;
            Dataset<Row> userDF = fetchAndConvertToDataFrame(endpoint, "users");
            userStoriesData.add(userDF);
        }

        return userStoriesData;
    }

    /**
     * Fetches the data in the given endpoint and convert it into dataframe
     *
     * @return A ArrayList of Datasets from each endpoint
     */
    public List<Dataset<Row>> handleEpics() {
        List<Dataset<Row>> epicsData = new ArrayList<>();

        for (Long projectId : projectIds) {
            String endpoint = EPICS.getPath() + "?project=" + projectId;
            Dataset<Row> epicsDF = fetchAndConvertToDataFrame(endpoint, "epics");

            if (epicsDF != null && !epicsDF.isEmpty()) {
                epicsData.add(epicsDF);
            }
        }

        return epicsData;
    }

    public Dataset<Row> handleIssues() {
        return fetchAndConvertToDataFrame(ISSUES.getPath(), "PLACEHOLDER");
    }

    /**
     * Authenticates a user with the Taiga API using the provided username and password.
     * Upon successful authentication, stores the received authentication token for subsequent API calls.
     *
     * @param username The username for Taiga account authentication
     * @param password The password for Taiga account authentication
     * @throws RuntimeException if authentication fails (non-200 status code) or if there's an error
     *         during the authentication process (e.g., connection issues, JSON parsing errors)
     * @throws NullPointerException if either username or password is null
     */
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
        authenticateTaiga("Man_Olito", "Manolito");
        String jsonResponse = utils.fetchDataFromEndpoint(TAIGA.getBaseUrl() + endpoint, authToken);
        Dataset<Row> data = utils.fetchDataAsDataFrame(jsonResponse);

        data = data.withColumn("tool_id", functions.lit(1));

        String mappedNameColumn = mapNameField(tableName);
        if (Arrays.asList(data.columns()).contains("name")) {
            data = data.withColumnRenamed("name", mappedNameColumn);
        }

        return data;
    }

    /**
     * Retrieves project IDs where the authenticated user is a member.
     * Uses the stored authentication token to fetch user information and associated projects.
     *
     * @throws IllegalStateException if no authentication token is available or if no projects are found
     *         for the authenticated user
     */
    private void getProjectWhereUserIsMember() {
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

        projectIds = projectsData.select("id")
                .as(Encoders.LONG())
                .collectAsList();
    }

    /**
     * Extracts the original user ID from a transformed user dataset.
     *
     * @param transformedUsers The dataset containing transformed user information
     * @return The original ID as a string, or null if the dataset is empty
     *
     * @throws NullPointerException if the transformedUsers parameter is null
     */
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
    }

    /**
     * Processes and saves user-role relationships to the database for all projects.
     * Extracts user-role-project associations, joins with dimension tables, and prepares the data for storage.
     *
     * @return A Dataset containing the final user-role-project relationships with their respective IDs
     * @throws RuntimeException if any error occurs during the processing or saving of user roles
     */
    public Dataset<Row> saveUserRoleToDatabase() {
        try {
            List<Dataset<Row>> tasksList = handleProjects();
            List<Dataset<Row>> processedUserRoles = new ArrayList<>();

            for (Dataset<Row> projectDF : tasksList) {
                Dataset<Row> userRolePairs = projectDF
                    .withColumn("member", explode(col("members")))
                    .select(
                            col("member.id").cast("long").as("user_original_id"),
                            col("member.role").cast("long").as("role_original_id"),
                            col("id").as("project_id")
                    );

                processedUserRoles.add(userRolePairs);
            }

            Dataset<Row> allUserRoles = processedUserRoles.stream()
                    .reduce(Dataset::union)
                    .orElse(spark.emptyDataFrame());

            Dataset<Row> roles = dataWarehouseLoader.loadDimensionWithoutTool("roles")
                    .select(
                            col("role_id").cast("long").as("role_id"),
                            col("original_id").cast("long").as("role_original_id")
                    );

            Dataset<Row> users = dataWarehouseLoader.loadDimensionWithoutTool("users")
                    .select(
                            col("user_id").cast("long").as("user_id"),
                            col("original_id").cast("long").as("user_original_id")
                    );

            return allUserRoles
                    .join(users, "user_original_id", "inner")
                    .join(roles, "role_original_id", "inner")
                    .select("project_id", "user_id", "role_id");

        } catch (Exception e) {
            throw new RuntimeException("Failed to save user roles", e);
        }
    }

    /**
     * Processes and saves task-tag relationships to the database.
     * Extracts task-tag associations from all tasks, joins with dimension tables,
     * and prepares the data for storage.
     *
     * @return A Dataset containing the final task-tag relationships with project, task, and tag IDs,
     *         ordered by project_id, task_id, and tag_id
     * @throws RuntimeException if any error occurs during the processing or saving of task tags
     */
    public Dataset<Row> saveTaskTagToDatabase() {
        try {
            List<Dataset<Row>> tasksList = handleTasks();
            List<Dataset<Row>> processedTaskTags = new ArrayList<>();

            for (Dataset<Row> tasksDF : tasksList) {
                Dataset<Row> taskTagPairs = tasksDF
                    .withColumn("tag", explode(col("tags")))
                    .select(
                            col("id").cast("long").as("task_original_id"),
                            col("tag").getItem(0).as("tag_name"),
                            col("project").cast("long").as("project_id")
                    );
                processedTaskTags.add(taskTagPairs);
            }

            Dataset<Row> allTaskTags = processedTaskTags.stream()
                    .reduce(Dataset::union)
                    .orElse(spark.emptyDataFrame());

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

            return allTaskTags
                    .join(tasks, "task_original_id", "inner")
                    .join(tags, "tag_name", "inner")
                    .select("project_id", "task_id", "tag_id")
                    .orderBy("project_id", "task_id", "tag_id");

        } catch (Exception e) {
            throw new RuntimeException("Failed to save task tags", e);
        }
    }

    /**
     * Joins the status dataset with projects dataset to map original project IDs to their corresponding database IDs.
     *
     * @param statusDF The dataset containing status information with original project IDs
     * @param projectsDF The dataset containing project information with original and new IDs
     * @return A new dataset with updated project IDs, containing original_id, status_name, and project_id columns
     * @throws NullPointerException if either statusDF or projectsDF is null
     */
    public static Dataset<Row> joinStatusProject(Dataset<Row> statusDF, Dataset<Row> projectsDF) {
        Dataset<Row> joined = statusDF
                .join(projectsDF, statusDF.col("project_id").equalTo(projectsDF.col("original_id")));
        return joined.select(
                statusDF.col("original_id"),
                statusDF.col("status_name"),
                projectsDF.col("project_id").alias("project_id")
        );
    }

    /**
     * Joins epics dataset with projects dataset to associate epics with their corresponding project IDs.
     *
     * @param epicsDF Dataset containing epic information with original project IDs
     * @param projectsDF Dataset containing project information with original and mapped IDs
     * @return Dataset containing epic original_id, mapped project_id, and epic_name
     * @throws NullPointerException if either epicsDF or projectsDF is null
     */
    public static Dataset<Row> joinEpicProject(Dataset<Row> epicsDF,
                                               Dataset<Row> projectsDF) {

        Dataset<Row> joinedWithProjects = epicsDF
                .join(projectsDF, epicsDF.col("project_id").equalTo(projectsDF.col("original_id")));
        return joinedWithProjects.select(
                epicsDF.col("original_id"),
                projectsDF.col("project_id"),
                epicsDF.col("epic_name")
        );
    }

    /**
     * Joins stories dataset with projects and epics datasets to associate stories with their corresponding project and epic IDs.
     *
     * @param storiesDF Dataset containing story information with original project and epic IDs
     * @param projectsDF Dataset containing project information with original and mapped IDs
     * @param epicsDF Dataset containing epic information with original IDs
     * @return Dataset containing story original_id, mapped project_id, epic_id, story_name, and is_finished
     * @throws NullPointerException if either storiesDF, projectsDF or epicsDF is null
     */
    public static Dataset<Row> joinStoryProjectAndEpic(Dataset<Row> storiesDF,
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

    /**
     * Joins task dataset with status, user, story and date datasets to create a consolidated fact table for tasks.
     *
     * @param tasksDF Dataset containing task information with original IDs
     * @param statusDF Dataset containing status information with original and mapped IDs
     * @param userDF Dataset containing user information with original and mapped IDs
     * @param storiesDF Dataset containing story information with original and mapped IDs
     * @param datesDF Dataset containing date dimension information
     * @return Dataset containing task original_id, status_id, assignee_id, tool_id, story_id, created_at,
     *         completed_at, due_date, task_name, is_blocked, and is_storyless
     * @throws NullPointerException if any of the input datasets is null
     */
    public static Dataset<Row> joinFactTask(Dataset<Row> tasksDF,
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

    /**
     * Executes the complete Taiga ETL process including authentication, data extraction,
     * transformation and loading of projects, tasks, epics and user stories.
     *
     * @throws RuntimeException if any error occurs during the ETL process
     * @throws IllegalStateException if authentication fails or no projects are found
     */
    //Remove post construct annotation after login is done
    @PostConstruct
    public void taigaEtl() {
        authenticateTaiga("Man_Olito", "Manolito");
        TaigaTransformer transformer = new TaigaTransformer(spark.emptyDataFrame());
        getProjectWhereUserIsMember();

        List<Dataset<Row>> projectsList = handleProjects();
        for (Dataset<Row> projectDF : projectsList) {
            Dataset<Row> transformedProject = transformer.transformProjects(projectDF);
            Dataset<Row> transformedRoles = transformer.transformRoles(projectDF);
            Dataset<Row> transformedUsers = transformer.transformedUserProjects(projectDF);

            dataWarehouseLoader.save(transformedProject, "projects");
            dataWarehouseLoader.save(transformedRoles, "roles");
            dataWarehouseLoader.save(transformedUsers, "users");
        }

        List<Dataset<Row>> tasksList = handleTasks();
        for (Dataset<Row> taskDF : tasksList) {
            Dataset<Row> transformedStatus = transformer.transformStatus(taskDF);
            transformedStatus = joinStatusProject(transformedStatus, dataWarehouseLoader.loadDimensionWithoutTool("projects"));
            Dataset<Row> transformedTags = transformer.transformTags(taskDF);

            dataWarehouseLoader.save(transformedStatus, "status");
            dataWarehouseLoader.save(transformedTags, "tags");
        }

        List<Dataset<Row>> epicsList = handleEpics();
        for (Dataset<Row> epicDF : epicsList) {
            Dataset<Row> transformedEpic = transformer.transformEpics(epicDF);
            transformedEpic = joinEpicProject(transformedEpic, dataWarehouseLoader.loadDimensionWithoutTool("projects"));
            dataWarehouseLoader.save(transformedEpic, "epics");
        }

        List<Dataset<Row>> storiesList = handleUserStories();
        for (Dataset<Row> storiesDF : storiesList) {
            Dataset<Row> transformedStories = transformer.transformUserStories(storiesDF);
            transformedStories = joinStoryProjectAndEpic(transformedStories, dataWarehouseLoader.loadDimensionWithoutTool("projects"),
                    dataWarehouseLoader.loadDimensionWithoutTool("epics")
            );
            dataWarehouseLoader.save(transformedStories, "stories");
        }

        for (Dataset<Row> factTaskDF : tasksList) {
            Dataset<Row> transformedFactTask = transformer.transformTasks(factTaskDF);
            transformedFactTask = joinFactTask(transformedFactTask,
                dataWarehouseLoader.loadDimension("status"),
                dataWarehouseLoader.loadDimension("users"),
                dataWarehouseLoader.loadDimension("stories"),
                dataWarehouseLoader.loadDimensionWithoutIsCurrent("dates", "taiga"));
            dataWarehouseLoader.save(transformedFactTask, "fact_tasks");
        }

        Dataset<Row> userRole = saveUserRoleToDatabase();
        dataWarehouseLoader.save(userRole,"user_role");

        Dataset<Row> taskTag = saveTaskTagToDatabase();
        dataWarehouseLoader.save(taskTag,"task_tag");
    }
}
