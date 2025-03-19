package com.manolito.dashflow.repository.dw;

import lombok.RequiredArgsConstructor;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.IntegerType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import static org.apache.spark.sql.functions.col;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final SparkSession spark;

    @Value("${spring.datasource.url}")
    private String jdbcUrl;

    @Value("${spring.datasource.username}")
    private String dbUser;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    @Override
    public Long getUserIdByOriginalId(String originalId) {
        try {
            Dataset<Row> users = spark.read()
                    .format("jdbc")
                    .option("url", jdbcUrl)
                    .option("dbtable", "dw_tasks.users")
                    .option("user", dbUser)
                    .option("password", dbPassword)
                    .load()
                    .filter(col("original_id").equalTo(originalId));

            return extractUserIdFromDataset(users);
        } catch (Exception e) {
            throw new RuntimeException("Error retrieving user_id for original_id: " + originalId, e);
        }
    }

    /**
     * Extracts the user_id from the Dataset.
     *
     * @param users The Dataset containing user data.
     * @return The user_id as a Long, or null if the Dataset is empty.
     */
    private Long extractUserIdFromDataset(Dataset<Row> users) {
        if (users.isEmpty()) {
            return null;
        }

        Row firstRow = users.select("user_id").head();
        if (firstRow.schema().apply("user_id").dataType() instanceof IntegerType) {
            return (long) firstRow.getInt(0);
        } else {
            return firstRow.getLong(0);
        }
    }
}
