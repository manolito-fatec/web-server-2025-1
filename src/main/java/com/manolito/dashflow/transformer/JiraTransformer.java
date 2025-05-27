package com.manolito.dashflow.transformer;

import lombok.RequiredArgsConstructor;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import static org.apache.spark.sql.functions.*;

@RequiredArgsConstructor
public class JiraTransformer {

    private static final int TOOL_ID = 3;
    private final Dataset<Row> datesDimension;

    public Dataset<Row> transformedUsers(Dataset<Row> rawData) {
        return rawData
                .filter(col("accountType").equalTo("atlassian"))
                .select(
                        col("accountId").as("original_id"),
                        lit(TOOL_ID).as("tool_id"),
                        col("displayName").as("user_name"),
                        col("project_id")
                );
    }

    public Dataset<Row> transformedProjects(Dataset<Row> rawData) {
        return rawData
                .select(
                        col("id").as("original_id"),
                        lit(TOOL_ID).as("tool_id"),
                        col("project_name")
                );
    }

    public Dataset<Row> transformedStatus(Dataset<Row> rawData) {
        return rawData
                .select(
                        col("statusCategory.id").as("original_id"),
                        when(col("statusCategory.name").equalTo("Itens Pendentes"), "New")
                                .when(col("statusCategory.name").equalTo("Em andamento"), "In Progress")
                                .when(col("statusCategory.name").equalTo("Itens concluídos"), "Closed")
                                .as("status_name"),
                        col("scope.project.id").as("project_id")
                )
                .distinct();
    }

    public Dataset<Row> transformedTags(Dataset<Row> rawData) {
        return rawData
                .select(
                        explode(col("issues")).as("issue")
                )
                .select(
                        explode(col("issue.fields.labels")).as("label"),
                        col("issue.fields.project.id").as("project_id")

                )
                .select(
                        col("label").as("original_id"),
                        col("label").as("tag_name"),
                        col("project_id")
                );
    }

    public Dataset<Row> transformedTasks(Dataset<Row> rawData) {
        return rawData
                .select(
                        explode(col("issues")).as("issue")
                )
                .select(
                        col("issue.key").as("original_id"),
                        col("issue.fields.summary").as("task_name"),
                        col("issue.fields.status.statusCategory.id").as("status_id"),
                        col("issue.fields.assignee.accountId").as("user_id"),
                        lit(TOOL_ID).as("tool_id"),
                        col("issue.fields.created").cast("date").as("created_at"),
                        when(col("issue.fields.resolutiondate").isNotNull(),
                                col("issue.fields.resolutiondate").cast("date")).otherwise(null).as("completed_at"),
                        when(col("issue.fields.duedate").isNotNull(),
                                col("issue.fields.duedate").cast("date")).otherwise(null).as("due_date"),
                        lit("0").as("epic_id"),
                        lit("0").as("story_id"),
                        lit(false).as("is_blocked"),
                        lit(true).as("is_storyless")
                );
    }
}