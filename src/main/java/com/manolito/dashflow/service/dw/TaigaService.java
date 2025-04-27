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
            case "issue_type" -> "type_name";
            case "issue_severity" -> "severity_name";
            case "issue_priority" -> "priority_name";
            case "fact_tasks" -> "task_name";
            default -> throw new IllegalArgumentException("Unsupported table for name field mapping: " + tableName);
        };
    }

    /**
     * Fetches project data from the API endpoint for each project ID and converts it into a DataFrame.
     * Each project's data is retrieved individually and added to the resulting list.
     *
     * @return List of Datasets containing project data, one Dataset per project ID
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
     * Fetches task data from the API endpoint for each project ID and converts it into a DataFrame.
     * The tasks are filtered by project ID in the API query parameters.
     *
     * @return List of Datasets containing task data, one Dataset per project ID
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
     * Fetches user story data from the API endpoint for each project ID and converts it into a DataFrame.
     * The user stories are filtered by project ID in the API query parameters.
     *
     * @return List of Datasets containing user story data, one Dataset per project ID
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
     * Fetches epic data from the API endpoint for each project ID and converts it into a DataFrame.
     * Only non-empty DataFrames are included in the result. Epics are filtered by project ID
     * in the API query parameters.
     *
     * @return List of non-empty Datasets containing epic data, one Dataset per project ID
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

    /**
     * Fetches issue data from the API endpoint for each project ID and converts it into a DataFrame.
     * Only non-empty DataFrames are included in the result. Issues are filtered by project ID
     * in the API query parameters.
     *
     * @return List of non-empty Datasets containing issue data, one Dataset per project ID
     */
    public List<Dataset<Row>> handleIssues() {
        List<Dataset<Row>> issuesData = new ArrayList<>();

        for (Long projectId : projectIds) {
            String endpoint = ISSUES.getPath() + "?project=" + projectId;
            Dataset<Row> issueDF = fetchAndConvertToDataFrame(endpoint, "issues");

            if (issueDF != null && !issueDF.isEmpty()) {
                issuesData.add(issueDF);
            }
        }

        return issuesData;
    }

    /**
     * Fetches issue type data from the API endpoint for each project ID and converts it into a DataFrame.
     * Only non-empty DataFrames are included in the result. Issue types are filtered by project ID
     * in the API query parameters.
     *
     * @return List of non-empty Datasets containing issue type data, one Dataset per project ID
     */
    public List<Dataset<Row>> handleIssueType() {
        List<Dataset<Row>> issueTypesData = new ArrayList<>();
        for (Long projectId : projectIds) {
            String endpoint = ISSUE_TYPES.getPath() + "?project=" + projectId;
            Dataset<Row> issueTypeDF = fetchAndConvertToDataFrame(endpoint, "issue_type");

            if (issueTypeDF != null && !issueTypeDF.isEmpty()) {
                issueTypesData.add(issueTypeDF);
            }
        }

        return issueTypesData;
    }

    /**
     * Fetches issue priority data from the API endpoint for each project ID and converts it into a DataFrame.
     * Only non-empty DataFrames are included in the result. Issue priorities are filtered by project ID
     * in the API query parameters.
     *
     * @return List of non-empty Datasets containing issue priority data, one Dataset per project ID
     */
    public List<Dataset<Row>> handleIssuePriority() {
        List<Dataset<Row>> issuePriorityData = new ArrayList<>();
        for (Long projectId : projectIds) {
            String endpoint = ISSUE_PRIORITY.getPath() + "?project=" + projectId;
            Dataset<Row> issuePriorityDF = fetchAndConvertToDataFrame(endpoint, "issue_priority");

            if (issuePriorityDF != null && !issuePriorityDF.isEmpty()) {
                issuePriorityData.add(issuePriorityDF);
            }
        }

        return issuePriorityData;
    }

    /**
     * Fetches issue severity data from the API endpoint for each project ID and converts it into a DataFrame.
     * Only non-empty DataFrames are included in the result. Issue severities are filtered by project ID
     * in the API query parameters.
     *
     * @return List of non-empty Datasets containing issue severity data, one Dataset per project ID
     */
    public List<Dataset<Row>> handleIssueSeverity() {
        List<Dataset<Row>> issueSeverityData = new ArrayList<>();
        for (Long projectId : projectIds) {
            String endpoint = ISSUE_SEVERITY.getPath() + "?project=" + projectId;
            Dataset<Row> issueSeverityDF = fetchAndConvertToDataFrame(endpoint, "issue_severity");

            if (issueSeverityDF != null && !issueSeverityDF.isEmpty()) {
                issueSeverityData.add(issueSeverityDF);
            }

        }

        return issueSeverityData;
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
     */
    public static Dataset<Row> joinEpicProject(Dataset<Row> epicsDF,
                                               Dataset<Row> projectsDF) {

        Dataset<Row> joinedDF = epicsDF
                .join(projectsDF, epicsDF.col("project_id").equalTo(projectsDF.col("original_id")));
        return joinedDF.select(
                epicsDF.col("original_id"),
                projectsDF.col("project_id"),
                epicsDF.col("epic_name")
        );
    }

    public static Dataset<Row> joinIssueProject(Dataset<Row> issuesDF,
                                                Dataset<Row> projectsDF,
                                                String name) {
        Dataset<Row> joinedDF = issuesDF
                .join(projectsDF, issuesDF.col("project_id").equalTo(projectsDF.col("original_id")));
        return joinedDF.select(
                issuesDF.col("original_id"),
                projectsDF.col("project_id"),
                issuesDF.col(name)
        );
    }

    /**
     * Joins tags dataset with projects dataset to associate tags with their corresponding project IDs.
     *
     * @param tagsDF Dataset containing tag information with original project IDs
     * @param projectsDF Dataset containing project information with original and mapped IDs
     * @return Dataset containing tag original_id, mapped project_id, and tag_name
     */
    public static Dataset<Row> joinTagProject(Dataset<Row> tagsDF,
                                              Dataset<Row> projectsDF) {

        Dataset<Row> joinedDF = tagsDF
                .join(projectsDF, tagsDF.col("project_id").equalTo(projectsDF.col("original_id")));
        return joinedDF.select(
                tagsDF.col("original_id"),
                projectsDF.col("project_id"),
                tagsDF.col("tag_name")
        );
    }

    /**
     * Joins stories dataset with projects and epics datasets to associate stories with their corresponding project and epic IDs.
     *
     * @param storiesDF Dataset containing story information with original project and epic IDs
     * @param projectsDF Dataset containing project information with original and mapped IDs
     * @param epicsDF Dataset containing epic information with original IDs
     * @return Dataset containing story original_id, mapped project_id, epic_id, story_name, and is_finished
     */
    public static Dataset<Row> joinStoryProjectAndEpic(Dataset<Row> storiesDF,
                                                       Dataset<Row> projectsDF,
                                                       Dataset<Row> epicsDF) {
        Dataset<Row> joinedDF = storiesDF
                .join(projectsDF, storiesDF.col("project_id").equalTo(projectsDF.col("original_id")))
                .join(epicsDF, storiesDF.col("epic_id").equalTo(epicsDF.col("original_id")), "left");

        return joinedDF.select(
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
     */
    public static Dataset<Row> joinFactTask(Dataset<Row> tasksDF,
                                            Dataset<Row> statusDF,
                                            Dataset<Row> userDF,
                                            Dataset<Row> storiesDF,
                                            Dataset<Row> datesDF) {

        Dataset<Row> joinedDF = tasksDF
                .join(statusDF, tasksDF.col("status_id").equalTo(statusDF.col("original_id")))
                .join(userDF, tasksDF.col("user_id").equalTo(userDF.col("original_id")))
                .join(storiesDF, tasksDF.col("story_id").equalTo(storiesDF.col("original_id")));

        joinedDF = mapDateColumn(joinedDF, datesDF, "created_at", "created_date_id");
        joinedDF = mapDateColumn(joinedDF, datesDF, "completed_at", "completed_date_id");
        joinedDF = mapDateColumn(joinedDF, datesDF, "due_date", "due_date_id");

        return joinedDF.select(
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
     * Joins issue dataset with status, user, project and date datasets to create a consolidated fact table for issues.
     *
     * @param issuesDF Dataset containing issue information with original IDs
     * @param statusDF Dataset containing status information with original and mapped IDs
     * @param userDF Dataset containing user information with original and mapped IDs
     * @param projectDF Dataset containing project information with original and mapped IDs
     * @param datesDF Dataset containing date dimension information
     * @return Dataset containing issue original_id, status_id, assignee_id, project_id, created_at,
     *         completed_at, and issue_name
     */
    public static Dataset<Row> joinFactIssue(Dataset<Row> issuesDF,
                                             Dataset<Row> statusDF,
                                             Dataset<Row> userDF,
                                             Dataset<Row> projectDF,
                                             Dataset<Row> issueTypeDF,
                                             Dataset<Row> issueSeverityDF,
                                             Dataset<Row> issuePriorityDF,
                                             Dataset<Row> datesDF) {

        Dataset<Row> joinedDF = issuesDF
                .join(statusDF, issuesDF.col("status_id").equalTo(statusDF.col("original_id")))
                .join(userDF, issuesDF.col("user_id").equalTo(userDF.col("original_id")))
                .join(projectDF, issuesDF.col("project_id").equalTo(projectDF.col("original_id")))
                .join(issueTypeDF, issuesDF.col("type_id").equalTo(issueTypeDF.col("original_id")))
                .join(issuePriorityDF, issuesDF.col("priority_id").equalTo(issuePriorityDF.col("original_id")))
                .join(issueSeverityDF, issuesDF.col("severity_id").equalTo(issueSeverityDF.col("original_id")));

        joinedDF = mapDateColumn(joinedDF, datesDF, "created_at", "created_date_id");
        joinedDF = mapDateColumn(joinedDF, datesDF, "completed_at", "completed_date_id");

        return joinedDF.select(
                issuesDF.col("original_id"),
                statusDF.col("status_id"),
                userDF.col("user_id").as("assignee_id"),
                projectDF.col("project_id"),
                issueTypeDF.col("type_id"),
                issueSeverityDF.col("severity_id"),
                issuePriorityDF.col("priority_id"),
                col("created_date_id").as("created_at"),
                col("completed_date_id").as("completed_at"),
                issuesDF.col("issue_name")
        );
    }

    /**
     * Helper method that maps a date column to its corresponding date dimension key.
     * Performs a left join with the date dimension table and renames the resulting date_id column.
     *
     * @param df Input DataFrame containing the date column to be mapped
     * @param datesDF Date dimension DataFrame (must include: date_date, date_id)
     * @param dateColumn Name of the date column in the input DataFrame to be mapped
     * @param outputColumn Name to be given to the resulting date dimension key column
     * @return DataFrame with the date column mapped to its dimension key
     */
    private static Dataset<Row> mapDateColumn(Dataset<Row> df, Dataset<Row> datesDF, String dateColumn, String outputColumn) {
        return df.join(datesDF,
                        df.col(dateColumn).cast("date").equalTo(datesDF.col("date_date")),
                        "left")
                .withColumnRenamed("date_id", outputColumn)
                .drop("date_date", "month", "year", "quarter", "day_of_week",
                        "day_of_month", "day_of_year", "is_weekend");
    }

    /**
     * Executes the complete Taiga ETL (Extract, Transform, Load) process.
     * The process includes:
     * Authentication with Taiga API
     * Data extraction for projects, tasks, epics, and user stories
     * Transformation of the extracted data
     * Loading the transformed data into the data warehouse
     * Processing of fact tables and relationships
     *
     *
     * @throws RuntimeException if any error occurs during the ETL process
     * @throws IllegalStateException if authentication fails or no projects are found
     */
    //Remove post construct annotation after login is done
    @PostConstruct
    public void taigaEtl() {
        try {
            authenticateTaiga("Man_Olito", "Manolito");

            TaigaTransformer transformer = new TaigaTransformer(spark.emptyDataFrame());
            getProjectWhereUserIsMember();

            processProjectsData(transformer);

            processTasksData(transformer);

            processEpicsData(transformer);

            processUserStoriesData(transformer);

            processFactTasks(transformer);

            processIssuesType(transformer);

            processIssuePriority(transformer);

            processIssueSeverity(transformer);

            processFactIssues(transformer);

            saveRelationshipData();
        } catch (Exception e) {
            throw new RuntimeException("Taiga ETL process failed", e);
        }
    }

    /**
     * Processes project-related data including projects, roles, and users.
     * Transforms the raw project data and saves it to the data warehouse.
     *
     * @param transformer The transformer instance to use for data transformation
     */
    private void processProjectsData(TaigaTransformer transformer) {
        List<Dataset<Row>> projectsList = handleProjects();
        for (Dataset<Row> projectDF : projectsList) {
            Dataset<Row> transformedProject = transformer.transformProjects(projectDF);
            Dataset<Row> transformedRoles = transformer.transformRoles(projectDF);
            Dataset<Row> transformedUsers = transformer.transformedUserProjects(projectDF);

            dataWarehouseLoader.save(transformedProject, "projects");
            dataWarehouseLoader.save(transformedRoles, "roles");
            dataWarehouseLoader.save(transformedUsers, "users");
        }
    }

    /**
     * Processes task-related data including status and tags.
     * Transforms the raw task data and saves it to the data warehouse.
     *
     * @param transformer The transformer instance to use for data transformation
     */
    private void processTasksData(TaigaTransformer transformer) {
        List<Dataset<Row>> tasksList = handleTasks();
        for (Dataset<Row> taskDF : tasksList) {
            Dataset<Row> transformedStatus = transformer.transformStatus(taskDF);
            transformedStatus = joinStatusProject(transformedStatus, dataWarehouseLoader.loadDimensionWithoutTool("projects"));
            Dataset<Row> transformedTags = transformer.transformTags(taskDF);
            transformedTags = joinTagProject(transformedTags, dataWarehouseLoader.loadDimensionWithoutTool("projects"));

            dataWarehouseLoader.save(transformedStatus, "status");
            dataWarehouseLoader.save(transformedTags, "tags");
        }
    }

    /**
     * Processes epic-related data.
     * Transforms the raw epic data and saves it to the data warehouse.
     *
     * @param transformer The transformer instance to use for data transformation
     */
    private void processEpicsData(TaigaTransformer transformer) {
        List<Dataset<Row>> epicsList = handleEpics();
        for (Dataset<Row> epicDF : epicsList) {
            Dataset<Row> transformedEpic = transformer.transformEpics(epicDF);
            transformedEpic = joinEpicProject(transformedEpic, dataWarehouseLoader.loadDimensionWithoutTool("projects"));
            dataWarehouseLoader.save(transformedEpic, "epics");
        }
    }

    /**
     * Processes user story-related data.
     * Transforms the raw user story data and saves it to the data warehouse.
     *
     * @param transformer The transformer instance to use for data transformation
     */
    private void processUserStoriesData(TaigaTransformer transformer) {
        List<Dataset<Row>> storiesList = handleUserStories();
        for (Dataset<Row> storiesDF : storiesList) {
            Dataset<Row> transformedStories = transformer.transformUserStories(storiesDF);
            transformedStories = joinStoryProjectAndEpic(transformedStories,
                    dataWarehouseLoader.loadDimensionWithoutTool("projects"),
                    dataWarehouseLoader.loadDimensionWithoutTool("epics")
            );
            dataWarehouseLoader.save(transformedStories, "stories");
        }
    }

    /**
     * Processes fact data for tasks.
     * Transforms the raw task data into fact format and saves it to the data warehouse.
     *
     * @param transformer The transformer instance to use for data transformation
     */
    private void processFactTasks(TaigaTransformer transformer) {
        List<Dataset<Row>> tasksList = handleTasks();
        for (Dataset<Row> factTaskDF : tasksList) {
            Dataset<Row> transformedFactTask = transformer.transformTasks(factTaskDF);
            transformedFactTask = joinFactTask(transformedFactTask,
                dataWarehouseLoader.loadDimension("status"),
                dataWarehouseLoader.loadDimension("users"),
                dataWarehouseLoader.loadDimension("stories"),
                dataWarehouseLoader.loadDimensionWithoutIsCurrent("dates", "taiga"));
            dataWarehouseLoader.save(transformedFactTask, "fact_tasks");
        }
    }

    /**
     * Processes issue type data.
     * Transforms the raw issue type data and saves it to the data warehouse.
     *
     * @param transformer The transformer instance to use for data transformation
     */
    private void processIssuesType(TaigaTransformer transformer) {
        List<Dataset<Row>> issuesList = handleIssueType();
        for (Dataset<Row> issueDF : issuesList) {
            Dataset<Row> transformedIssueType = transformer.transformIssueTypes(issueDF);
            transformedIssueType = joinIssueProject(transformedIssueType,
                    dataWarehouseLoader.loadDimensionWithoutTool("projects"), "type_name");
            dataWarehouseLoader.save(transformedIssueType, "issue_type");
        }
    }

    /**
     * Processes issue priority data.
     * Transforms the raw issue priority data and saves it to the data warehouse.
     *
     * @param transformer The transformer instance to use for data transformation
     */
    private void processIssuePriority(TaigaTransformer transformer) {
        List<Dataset<Row>> issuePriorityList = handleIssuePriority();
        for (Dataset<Row> issuePriorityDF : issuePriorityList) {
            Dataset<Row> transformedIssuePriority = transformer.transformIssuePriority(issuePriorityDF);
            transformedIssuePriority = joinIssueProject(transformedIssuePriority,
                    dataWarehouseLoader.loadDimensionWithoutTool("projects"), "priority_name");
            dataWarehouseLoader.save(transformedIssuePriority, "issue_priority");
        }
    }

    /**
     * Processes issue severity data.
     * Transforms the raw issue severity data and saves it to the data warehouse.
     *
     * @param transformer The transformer instance to use for data transformation
     */
    private void processIssueSeverity(TaigaTransformer transformer) {
        List<Dataset<Row>> issueSeverityList = handleIssueSeverity();
        for (Dataset<Row> issueSeverityDF : issueSeverityList) {
            Dataset<Row> transformedIssueSeverity = transformer.transformIssueSeverity(issueSeverityDF);
            transformedIssueSeverity = joinIssueProject(transformedIssueSeverity,
                    dataWarehouseLoader.loadDimensionWithoutTool("projects"), "severity_name");
            dataWarehouseLoader.save(transformedIssueSeverity, "issue_severity");
        }
    }

    /**
     * Processes fact data for issues.
     * Transforms the raw issue data into fact format and saves it to the data warehouse.
     *
     * @param transformer The transformer instance to use for data transformation
     */
    private void processFactIssues(TaigaTransformer transformer) {
        List<Dataset<Row>> issuesList = handleIssues();
        for (Dataset<Row> factIssueDF : issuesList) {
            Dataset<Row> transformedStatusIssues = transformer.transformStatus(factIssueDF);
            transformedStatusIssues = joinStatusProject(transformedStatusIssues, dataWarehouseLoader.loadDimensionWithoutTool("projects"));
            dataWarehouseLoader.save(transformedStatusIssues, "issue_status");

            Dataset<Row> transformedFactIssue = transformer.transformIssues(factIssueDF);
            transformedFactIssue = joinFactIssue(transformedFactIssue,
                    dataWarehouseLoader.loadDimension("issue_status"),
                    dataWarehouseLoader.loadDimension("users"),
                    dataWarehouseLoader.loadDimensionWithoutTool("projects"),
                    dataWarehouseLoader.loadDimension("issue_type"),
                    dataWarehouseLoader.loadDimension("issue_severity"),
                    dataWarehouseLoader.loadDimension("issue_priority"),
                    dataWarehouseLoader.loadDimensionWithoutIsCurrent("dates", "taiga"));
            dataWarehouseLoader.save(transformedFactIssue, "fact_issues");
        }
    }

    /**
     * Saves relationship data to the data warehouse.
     * Processes and saves user-role and task-tag relationships by purging and then populating the table with new data.
     */
    private void saveRelationshipData() {
        dataWarehouseLoader.purgeTable("user_role");
        dataWarehouseLoader.purgeTable("task_tag");

        Dataset<Row> userRole = saveUserRoleToDatabase();
        dataWarehouseLoader.save(userRole,"user_role");

        Dataset<Row> taskTag = saveTaskTagToDatabase();
        dataWarehouseLoader.save(taskTag,"task_tag");
    }
}
