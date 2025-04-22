package com.manolito.dashflow.util;

import lombok.RequiredArgsConstructor;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.spark.sql.*;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.manolito.dashflow.enums.Schema.DATAWAREHOUSE;

@Component
@RequiredArgsConstructor
public class SparkUtils {

    private final SparkSession spark;

    /**
     * Fetches data from an external API endpoint.
     *
     * @param url       The URL of the API endpoint.
     * @param authToken The authorization token.
     * @return The response data as a JSON string.
     */
    public String fetchDataFromEndpoint(String url, String authToken) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(url);

            request.addHeader("Authorization", "Bearer " + authToken);
            request.addHeader("Accept", "application/json");

            HttpResponse response = httpClient.execute(request);

            if (response.getStatusLine().getStatusCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : " + response.getStatusLine().getStatusCode());
            }

            return EntityUtils.toString(response.getEntity());
        } catch (Exception e) {
            throw new RuntimeException("Error fetching data from endpoint: " + url, e);
        }
    }

    /**
     * Converts a JSON response into a Spark DataFrame.
     *
     * @param jsonResponse The JSON response as a string.
     * @return A DataFrame containing the parsed JSON data.
     */
    public Dataset<Row> fetchDataAsDataFrame(String jsonResponse) {
        return spark.read().json(spark.createDataset(List.of(jsonResponse), Encoders.STRING()));
    }

    /**
     * Fetches the column names of the specified table from the database, excluding SCD2-managed columns.
     *
     * @param jdbcUrl    The JDBC URL of the database.
     * @param dbUser     The database username.
     * @param dbPassword The database password.
     * @param tableName  The name of the table.
     * @return A list of column names (excluding SCD2-managed columns).
     */
    public List<String> fetchTableColumns(String jdbcUrl, String dbUser, String dbPassword, String tableName) {
        List<String> columns = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(jdbcUrl, dbUser, dbPassword);
             Statement statement = connection.createStatement()) {

            String query = String.format(
                    "SELECT column_name " +
                            "FROM information_schema.columns " +
                            "WHERE table_schema = '" + DATAWAREHOUSE.getSchema() + "' AND table_name = '%s'",
                    tableName);

            ResultSet resultSet = statement.executeQuery(query);
            while (resultSet.next()) {
                String columnName = resultSet.getString("column_name");

                // Exclude SCD2-managed columns
                if (!isScd2ManagedColumn(columnName)) {
                    columns.add(columnName);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error fetching columns for table: " + tableName, e);
        }
        return columns;
    }

    /**
     * Checks if a column is managed by SCD2 logic (e.g., seq, start_date, end_date, is_current, is_active).
     *
     * @param columnName The name of the column.
     * @return True if the column is managed by SCD2 logic, false otherwise.
     */
    private boolean isScd2ManagedColumn(String columnName) {
        return columnName.equals("seq") ||
                columnName.equals("start_date") ||
                columnName.equals("end_date") ||
                columnName.equals("is_current");
    }

    /**
     * Filters and returns the columns that exist in both the DataFrame and the target table,
     * applying a hardcoded column mapping for "id" -> "original_id".
     *
     * @param data         The DataFrame.
     * @param tableColumns The columns of the target table.
     * @return An array of columns to select.
     */
    public Column[] getColumns(Dataset<Row> data, List<String> tableColumns) {
        List<Column> columns = new ArrayList<>();
        List<String> dataColumns = Arrays.asList(data.columns()); // Convert array to list for .contains

        for (String dbColumn : tableColumns) {
            // Handle the "id" -> "original_id" mapping
            String dataFrameColumn = dbColumn.equals("id") ? "original_id" : dbColumn;

            if (dataColumns.contains(dataFrameColumn)) {
                columns.add(functions.col(dataFrameColumn).alias(dbColumn)); // Alias to match database column name
            }
        }
        return columns.toArray(new Column[0]);
    }
}