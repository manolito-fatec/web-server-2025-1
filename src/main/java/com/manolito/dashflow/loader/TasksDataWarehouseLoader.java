package com.manolito.dashflow.loader;

import lombok.RequiredArgsConstructor;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.SparkSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.apache.spark.sql.functions.col;

/**
 * This class is responsible for loading and managing data from the data warehouse (DW) using Apache Spark.
 * <p>
 * The class manages tools and date dimensions, performs caching, and provides methods for saving data back to the database.
 * It supports retrieving tool metadata and dimensions based on specific tool names.
 * </p>
 */
@Component
@RequiredArgsConstructor
public class TasksDataWarehouseLoader {

    private final SparkSession spark;
    private Map<String, ToolMetadata> toolCache = new ConcurrentHashMap<>();
    private Dataset<Row> cachedDates;
    private Dataset<Row> cachedTools;

    @Value("${spring.datasource.url}")
    private String jdbcUrl;

    @Value("${spring.datasource.username}")
    private String dbUser;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    /**
     * Initializes the loader by loading tools and caching common dimensions during server startup.
     */
    @PostConstruct
    public void initialize() {
        loadAllTools();
        cacheCommonDimensions();
    }

    /**
     * Retrieves the tool ID for the specified tool name.
     *
     * @param toolName The name of the tool.
     * @return The tool ID.
     * @throws IllegalArgumentException if the tool name is not found.
     */
    public int getToolId(String toolName) {
        ToolMetadata meta = toolCache.get(toolName.toLowerCase());
        if (meta == null) {
            throw new IllegalArgumentException("Tool not found: " + toolName +
                    ". Available tools: " + String.join(", ", toolCache.keySet()));
        }
        return meta.toolId;
    }

    /**
     * @return The cached tools dimension dataset.
     */
    public Dataset<Row> getToolsDimension() {
        return cachedTools;
    }

    /**
     * Loads a specific dimension dataset filtered by the given tool name.
     *
     * @param tableName The name of the dimension table.
     * @param toolName  The tool name to filter by.
     * @return The filtered dimension dataset.
     */
    public Dataset<Row> loadDimension(String tableName, String toolName) {
        int toolId = getToolId(toolName);
        return spark.read()
                .format("jdbc")
                .option("url", jdbcUrl)
                .option("dbtable", "dw_tasks." + tableName)
                .option("user", dbUser)
                .option("password", dbPassword)
                .load()
                .filter(col("tool_id").equalTo(toolId))
                .filter(col("is_current").equalTo(true));
    }

    /**
     * Saves the provided dataset to the specified table in the data warehouse.
     *
     * @param data      The dataset to save.
     * @param tableName The name of the target table.
     */
    public void save(Dataset<Row> data, String tableName) {
        data.write()
                .format("jdbc")
                .option("url", jdbcUrl)
                .option("dbtable", "dw_tasks." + tableName)
                .option("user", dbUser)
                .option("password", dbPassword)
                .option("batchsize", 10000)
                .mode(SaveMode.Append)
                .save();
    }

    /**
     * Saves the provided dataset with retries if an error occurs during the save operation.
     *
     * @param data      The dataset to save.
     * @param tableName The name of the target table.
     * @param maxRetries The maximum number of retries in case of failure.
     */
    public void saveWithRetries(Dataset<Row> data, String tableName, int maxRetries) {
        int attempts = 0;
        while (attempts < maxRetries) {
            try {
                save(data, tableName);
                break;
            } catch (Exception e) {
                attempts++;
                e.printStackTrace();
            }
        }
    }

    /**
     * A simple utility class to hold metadata information of a tool.
     */
    private static class ToolMetadata {
        final int toolId;
        final String toolName;
        final boolean isActive;

        ToolMetadata(Row row) {
            this.toolId = row.getInt(row.fieldIndex("tool_id"));
            this.toolName = row.getString(row.fieldIndex("tool_name"));
            this.isActive = row.getBoolean(row.fieldIndex("is_active"));
        }
    }

    /**
     * Loads all currently active tools from the "dw_tasks.tools" table, caches them, and populates the toolCache.
     */
    private void loadAllTools() {
        Dataset<Row> tools = spark.read()
                .format("jdbc")
                .option("url", jdbcUrl)
                .option("dbtable", "dw_tasks.tools")
                .option("user", dbUser)
                .option("password", dbPassword)
                .load()
                .filter(col("is_current").equalTo(true));

        this.cachedTools = tools.cache();

        List<Row> toolRows = tools.collectAsList();
        toolCache.clear();

        for (Row row : toolRows) {
            ToolMetadata meta = new ToolMetadata(row);
            toolCache.put(meta.toolName.toLowerCase(), meta);
        }

        if (toolCache.isEmpty()) {
            System.out.println("No tools loaded");
//            throw new IllegalStateException("No tools found in tools table");
        }
    }

    /**
     * Caches common static dimensions (tools and dates) from the data warehouse.
     */
    private void cacheCommonDimensions() {
        this.cachedTools = loadDimension("tools").cache();

        this.cachedDates = spark.read()
                .format("jdbc")
                .option("url", jdbcUrl)
                .option("dbtable", "dw_tasks.dates")
                .option("user", dbUser)
                .option("password", dbPassword)
                .load()
                .cache();
    }

    /**
     * Loads a specific dimension dataset.
     *
     * @param tableName The name of the dimension table.
     * @return The dimension dataset.
     */
    private Dataset<Row> loadDimension(String tableName) {
        return spark.read()
                .format("jdbc")
                .option("url", jdbcUrl)
                .option("dbtable", "dw_tasks." + tableName)
                .option("user", dbUser)
                .option("password", dbPassword)
                .load()
                .filter(col("is_current").equalTo(true));
    }
}
