package com.manolito.dashflow.transformer;

import lombok.RequiredArgsConstructor;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import static org.apache.spark.sql.functions.*;

@RequiredArgsConstructor
public class JiraTransformer {

    private final Dataset<Row> datesDimension;

    public Dataset<Row> transformerUsers(Dataset<Row> rawData) {
        return rawData
                .filter(col("accountType").equalTo("atlassian"))
                .select(
                        col("accountId").alias("original_id"),
                        col("displayName").alias("user_name")
                );
    }
}