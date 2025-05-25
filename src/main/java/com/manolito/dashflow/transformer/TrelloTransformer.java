package com.manolito.dashflow.transformer;

import lombok.RequiredArgsConstructor;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import static org.apache.spark.sql.functions.*;

@RequiredArgsConstructor
public class TrelloTransformer {

    private static final int TOOL_ID = 2;
    private final Dataset<Row> datesDimension;

    public Dataset<Row> transformProjects(Dataset<Row> rawData) {
        return rawData.select(
                col("id").as("original_id"),
                lit(TOOL_ID).as("tool_id"),
                col("displayName").as("project_name"),
                col("desc").as("description")
        );
    }

    public Dataset<Row> transformUsers(Dataset<Row> rawData) {
        return rawData.select(
                col("id").as("original_id"),
                lit(TOOL_ID).as("tool_id"),
                col("fullName").as("user_name")
        );
    }

    public Dataset<Row> transformStatus(Dataset<Row> rawData) {
        return rawData.select(
                col("id").as("original_id"),
                col("status_name")
        );
    }

    public Dataset<Row> transformTags(Dataset<Row> rawData) {
        return rawData.select(
                col("id").as("original_id"),
                col("tag_name")
        );
    }

    public Dataset<Row> transformCards(Dataset<Row> rawData) {
        Dataset<Row> withMembers = rawData.filter(size(col("idMembers")).gt(0));

        Dataset<Row> exploded = withMembers
                .withColumn("member_id", explode(col("idMembers")));

        Dataset<Row> withoutMembers = rawData.filter(size(col("idMembers")).leq(0))
                .withColumn("member_id", lit(null));

        return exploded.unionByName(withoutMembers)
                .select(
                        col("id").as("original_id"),
                        col("task_name"),
                        col("desc").as("description"),
                        col("member_id").as("assignee_id"),
                        lit(TOOL_ID).as("tool_id")
                );
    }
}