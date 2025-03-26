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
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.functions;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.LongType;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
        return fetchAndConvertToDataFrame(TASKS.getPath(), "fact_tasks");
    }

    public Dataset<Row> handleIssues() {
        return fetchAndConvertToDataFrame(ISSUES.getPath(), "PLACEHOLDER");
    }

    public Dataset<Row> handleEpics() {
        return fetchAndConvertToDataFrame(EPICS.getPath(), "epics");
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

        saveOrUpdateUser(transformedUsers, originalId);

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

    private void saveOrUpdateUser(Dataset<Row> transformedUsers, String originalId) {
        Long userId = userRepository.getUserIdByOriginalId(originalId);

        if (userId != null) {
            System.out.println("User already exists with user_id: " + userId);
        } else {
            dataWarehouseLoader.save(transformedUsers, "users");
            System.out.println("User data saved.");
        }
    }
    private Map<Long, Long> mapFromLongList(List<Long[]> list) {
        return list.stream()
                .collect(Collectors.toMap(
                        pair -> pair[1],
                        pair -> pair[0]
                ));
    };

    //@PostConstruct
    public void saveUserRoleToDatabase() {
        project = String.valueOf(1637322);
        try{
            TaigaTransformer transformer = new TaigaTransformer(spark.emptyDataFrame());
            Dataset<Row> jsonFromEtl = handleProjects().withColumn("member",explode(col("members")))
                    .select(
                            col("member.role").as("oRole_id"),
                            col("member.id").as("oUser_id"));
            List<Long[]> userRoleSparkMap = jsonFromEtl.collectAsList().stream().map(row -> new Long[]{
                    row.getAs("oRole_id"),
                    row.getAs("oUser_id")
            }).toList();

            Dataset<Row> roleFromSpark = dataWarehouseLoader.loadDimension("roles","taiga");
            List<Long[]> roleIdListFromDb = (roleFromSpark.select("role_id","original_id")).collectAsList().stream().map(row -> new Long[]{
                    Long.valueOf(row.getAs("role_id").toString()),
                    Long.valueOf(row.getAs("original_id").toString())
            }).toList();

            Dataset<Row> usersFromDb = dataWarehouseLoader.loadDimension("users","taiga");
            List<Long[]> usersIdListFromDb = (usersFromDb.select("user_id","original_id")).collectAsList().stream().map(row -> new Long[]{
                    Long.valueOf(row.getAs("user_id").toString()),
                    Long.valueOf(row.getAs("original_id").toString())
            }).toList();

            Map<Long, Long> roleMap = mapFromLongList(roleIdListFromDb);

            Map<Long, Long> userMap = mapFromLongList(usersIdListFromDb);

            List<Long[]> finalList = userRoleSparkMap.stream()
                    .map(pair -> {
                        Long roleOriginal = pair[0];
                        Long userOriginal = pair[1];

                        Long roleIdFromDb = roleMap.get(roleOriginal);
                        Long userIdFromDb = userMap.get(userOriginal);

                        if (roleIdFromDb != null && userIdFromDb != null) {
                            return new Long[]{ userIdFromDb, roleIdFromDb };
                        } else {
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .toList();

            List<Row> rows = finalList.stream()
                    .map(pair -> RowFactory.create(pair[0], pair[1]))
                    .collect(Collectors.toList());

            StructType schema = DataTypes.createStructType(new StructField[]{
                    DataTypes.createStructField("user_id", DataTypes.LongType, false),
                    DataTypes.createStructField("role_id", DataTypes.LongType, false)
            });

            Dataset<Row> finalDataset = spark.createDataFrame(rows, schema);

            dataWarehouseLoader.save(finalDataset, "user_role");


        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static Dataset<Row> updateStatusProjectId(Dataset<Row> statusDF, Dataset<Row> projectsDF) {
        Dataset<Row> joined = statusDF
                .join(projectsDF, statusDF.col("project_id").equalTo(projectsDF.col("original_id")));

        return joined.select(
                statusDF.col("original_id"),
                statusDF.col("tool_id"),
                statusDF.col("status_name"),
                projectsDF.col("project_id").alias("project_id")
        );
    }
    //Remove post construct annotation after login is done
//    @PostConstruct
    public void taigaEtl() {
        //authenticateTaiga("gabguska", "aluno123");
        TaigaTransformer transformer = new TaigaTransformer(spark.emptyDataFrame());

        //Dataset<Row> roles = transformer.transformRoles(handleRoles());
        //Dataset<Row> userRoles = transformer.transformUserRole(handleRoles());
//      userole
        //Dataset<Row> projects = transformer.transformProjects(handleProjects());
        Dataset<Row> status = transformer.transformStatus(handleStatus());
        status = updateStatusProjectId(status, dataWarehouseLoader.loadDimension("projects","taiga"));
        dataWarehouseLoader.save(status, "status");

//      tags
          //dataWarehouseLoader.save(projects, "projects");
          //dataWarehouseLoader.save(status, "status");
//      tasks
//
    }
}
