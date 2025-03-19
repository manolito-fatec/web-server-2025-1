package com.manolito.dashflow.transformer;

import lombok.RequiredArgsConstructor;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import static org.apache.spark.sql.functions.*;

@RequiredArgsConstructor
public class TaigaTransformer {

    private static final int TOOL_ID = 1; // Change for a GET from tools table later when implemented
    private final Dataset<Row> datesDimension;

    public Dataset<Row> transformProjects(Dataset<Row> rawData) {
        return rawData.select(
                col("id").as("original_id"),
                lit(TOOL_ID).as("tool_id"),
                col("name").as("project_name"),
                col("description"),
                current_date().as("start_date"),
                lit(false).as("is_finished"),
                lit(true).as("is_active")
        );
    }

    public Dataset<Row> transformUserStories(Dataset<Row> rawUserStories) {
        return rawUserStories.select(
                col("id").as("original_id"),
                lit(TOOL_ID).as("tool_id"),
                col("project").as("project_id"),
                col("epic").as("epic_id"),
                col("subject").as("story_name"),
                col("description"),
                col("is_closed").as("is_finished"),
                lit(true).as("is_active")
        );
    }

    public Dataset<Row> transformIssues(Dataset<Row> rawIssues) {
        return rawIssues.select(
                col("id").as("original_id"),
                col("status").as("status_id"),
                col("owner").as("owner_id"),
                lit(TOOL_ID).as("tool_id"),
                col("project").as("project_id"),
                to_date(col("created_date")).as("created_at"),
                to_date(col("modified_date")).as("updated_at"),
                col("subject").as("issue_name"),
                col("description"),
                col("is_blocked"),
                col("is_closed").as("is_resolved")
        );
    }

    public Dataset<Row> transformUserStoryStatuses(Dataset<Row> rawStatuses) {
        return rawStatuses.select(
                col("id").as("original_id"),
                lit(TOOL_ID).as("tool_id"),
                col("project").as("project_id"),
                col("name").as("status_name"),
                col("color").as("status_color"),
                lit(true).as("is_active")
        );
    }

    public Dataset<Row> transformUsers(Dataset<Row> rawUsers) {
        return rawUsers.select(
                col("id").as("original_id"),
                lit(TOOL_ID).as("tool_id"),
                col("username").as("user_name"),
                col("email"),
                col("bio").as("description"),
                lit(true).as("is_active")
        );
    }

    public Dataset<Row> transformRoles(Dataset<Row> rawRoles) {
        return rawRoles.select(
                col("id").as("original_id"),
                lit(TOOL_ID).as("tool_id"),
                col("name").as("role_name"),
                lit(true).as("is_active")
        );
    }

}
