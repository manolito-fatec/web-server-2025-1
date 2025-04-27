package com.manolito.dashflow.loader;

import com.manolito.dashflow.util.SparkUtils;
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

import static com.manolito.dashflow.enums.Schema.DATAWAREHOUSE;
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
    private final SparkUtils sparkUtils;
    private Map<String, ToolMetadata> toolCache = new ConcurrentHashMap<>();
    private Dataset<Row> cachedDates;
    private Dataset<Row> cachedTools;

    @Value("${spring.datasource.url}")
    public String jdbcUrl;

    @Value("${spring.datasource.username}")
    public String dbUser;

    @Value("${spring.datasource.password}")
    public String dbPassword;

    @PostConstruct
    public void initialize() {
        loadAllTools();
        cacheCommonDimensions();
    }

    public int getToolId(String toolName) {
        ToolMetadata meta = toolCache.get(toolName.toLowerCase());
        if (meta == null) {
            throw new IllegalArgumentException("Tool not found: " + toolName +
                    ". Available tools: " + String.join(", ", toolCache.keySet()));
        }
        return meta.toolId;
    }

    public Dataset<Row> getToolsDimension() {
        return cachedTools;
    }

    public Dataset<Row> loadDimension(String tableName, String toolName) {
        int toolId = getToolId(toolName);
        return spark.read()
                .format("jdbc")
                .option("url", jdbcUrl)
                .option("dbtable", DATAWAREHOUSE.getSchema()+ "." + tableName)
                .option("user", dbUser)
                .option("password", dbPassword)
                .load()
                .filter(col("tool_id").equalTo(toolId))
                .filter(col("is_current").equalTo(true));
    }

    public Dataset<Row> loadDimensionWithoutTool(String tableName) {
        return spark.read()
                .format("jdbc")
                .option("url", jdbcUrl)
                .option("dbtable", DATAWAREHOUSE.getSchema() + "." + tableName)
                .option("user", dbUser)
                .option("password", dbPassword)
                .load()
                .filter(col("is_current").equalTo(true));
    }

    public Dataset<Row> loadDimensionWithoutIsCurrent(String tableName, String toolName) {
        return spark.read()
                .format("jdbc")
                .option("url", jdbcUrl)
                .option("dbtable", DATAWAREHOUSE.getSchema() + "." + tableName)
                .option("user", dbUser)
                .option("password", dbPassword)
                .load();
    }

    public void save(Dataset<Row> data, String tableName) {
        try {
            List<String> tableColumns = sparkUtils.fetchTableColumns(jdbcUrl, dbUser, dbPassword, tableName);
            Dataset<Row> filteredData = data.select(sparkUtils.getColumns(data, tableColumns));

            SaveMode mode = isLinkTable(tableName) ? SaveMode.Overwrite : SaveMode.Append;

            filteredData.write()
                    .format("jdbc")
                    .option("url", jdbcUrl)
                    .option("dbtable", DATAWAREHOUSE.getSchema() + "." + tableName)
                    .option("user", dbUser)
                    .option("password", dbPassword)
                    .option("batchsize", 10000)
                    .mode(mode)
                    .save();
        } catch (Exception e) {
            throw new RuntimeException("Error saving data to table: " + tableName, e);
        }
    }

    private boolean isLinkTable(String tableName) {
        return tableName.equals("user_role") || tableName.equals("task_tag");
    }

    public void truncateTable(String tableName) {
        String fullTableName = "dw_tasks." + tableName;
        try {
            if (spark.catalog().tableExists("dw_tasks", tableName)) {
                spark.sql("TRUNCATE TABLE " + fullTableName);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to truncate table " + tableName, e);
        }
    }

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

    private static class ToolMetadata {
        final int toolId;
        final String toolName;

        ToolMetadata(Row row) {
            this.toolId = row.getInt(row.fieldIndex("tool_id"));
            this.toolName = row.getString(row.fieldIndex("tool_name"));
        }
    }

    private void loadAllTools() {
        Dataset<Row> tools = spark.read()
                .format("jdbc")
                .option("url", jdbcUrl)
                .option("dbtable", DATAWAREHOUSE.getSchema() + ".tools")
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
        }
    }

    private void cacheCommonDimensions() {
        this.cachedTools = loadDimension("tools").cache();

        this.cachedDates = spark.read()
                .format("jdbc")
                .option("url", jdbcUrl)
                .option("dbtable", DATAWAREHOUSE.getSchema() + ".dates")
                .option("user", dbUser)
                .option("password", dbPassword)
                .load()
                .cache();
    }

    public Dataset<Row> loadDimension(String tableName) {
        return spark.read()
                .format("jdbc")
                .option("url", jdbcUrl)
                .option("dbtable", DATAWAREHOUSE.getSchema() + "." + tableName)
                .option("user", dbUser)
                .option("password", dbPassword)
                .load()
                .filter(col("is_current").equalTo(true));
    }

    public void purgeTable(String tableName) {
        try {
            spark.read()
                    .format("jdbc")
                    .option("url", jdbcUrl)
                    .option("dbtable", DATAWAREHOUSE.getSchema() + "." + tableName)
                    .option("user", dbUser)
                    .option("password", dbPassword)
                    .load()
                    .write()
                    .format("jdbc")
                    .option("url", jdbcUrl)
                    .option("dbtable", DATAWAREHOUSE.getSchema() + "." + tableName)
                    .option("user", dbUser)
                    .option("password", dbPassword)
                    .mode(SaveMode.Overwrite)
                    .save();
        } catch (Exception e) {
            throw new RuntimeException("Error purging table: " + tableName, e);
        }
    }
}
