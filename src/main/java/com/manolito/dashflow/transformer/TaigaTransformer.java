package com.manolito.dashflow.transformer;

import lombok.RequiredArgsConstructor;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import static org.apache.spark.sql.functions.*;

@RequiredArgsConstructor
public class TaigaTransformer {

    private static final int TOOL_ID = 1; // Change for a GET from tools table later when implemented
    private static final int EPIC_ID = 1;
    private final Dataset<Row> datesDimension;

    public Dataset<Row> transformProjects(Dataset<Row> rawData) {
        return rawData.select(
                col("id").as("original_id"),
                lit(TOOL_ID).as("tool_id"),
                col("description").as("project_name"),
                col("description"),
                current_date().as("start_date"),
                lit(false).as("is_finished")
        );
    }

    public Dataset<Row> transformUserStories(Dataset<Row> rawUserStories) {
        return rawUserStories.select(
                col("id").as("original_id"),
                col("project").as("project_id"),
                lit(EPIC_ID).as("epic_id"),
                col("subject").as("story_name"),
                col("is_closed").as("is_finished")
        );
    }

    public Dataset<Row> transformIssues(Dataset<Row> rawIssues) {
        return rawIssues.select(
                col("id").as("original_id"),
                col("status").as("status_id"),
                col("owner").as("user_id"),
                col("project").as("project_id"),
                col("severity").as("severity_id"),
                col("priority").as("priority_id"),
                col("type").as("type_id"),
                (col("created_date")).cast("date").as("created_at"),
                (col("modified_date")).cast("date").as("completed_at"),
                col("subject").as("issue_name")
        );
    }

    public Dataset<Row> transformIssueTypes(Dataset<Row> rawIssueTypes) {
        return rawIssueTypes.select(
                col("id").as("original_id"),
                col("type_name"),
                col("project").as("project_id")
        );
    }

    public Dataset<Row> transformIssueSeverity(Dataset<Row> rawIssueSeverities) {
        return rawIssueSeverities.select(
                col("id").as("original_id"),
                col("severity_name"),
                col("project").as("project_id")
        );
    }

    public Dataset<Row> transformIssuePriority(Dataset<Row> rawIssuePriority) {
        return rawIssuePriority.select(
                col("id").as("original_id"),
                col("priority_name"),
                col("project").as("project_id")
        );
    }

    public Dataset<Row> transformUsers(Dataset<Row> rawUsers) {
        return rawUsers.select(
                col("id").as("original_id"),
                lit(TOOL_ID).as("tool_id"),
                col("full_name").as("user_name"),
                col("email"),
                col("bio").as("description")
        );
    }

    public Dataset<Row> transformedUserProjects(Dataset<Row> rawUserProjects) {
        return rawUserProjects
                .withColumn("member", explode(col("members")))
                .select(
                        col("member.id").as("original_id"),
                        lit(TOOL_ID).as("tool_id"),
                        col("member.full_name").as("user_name")
                );
    }

    public Dataset<Row> transformRoles(Dataset<Row> rawRoles) {
        return rawRoles
                .withColumn("role", explode(col("roles")))
                .select(
                        col("role.id").as("original_id"),
                        lit(TOOL_ID).as("tool_id"),
                        col("role.name").as("role_name")
                )
                .distinct();
    }

    public Dataset<Row> transformUserRole(Dataset<Row> rawUserRoles) {
        return rawUserRoles
                .withColumn("member", explode(col("members")))
                .select(
                        col("member.role").as("role_id"),
                        col("member.id").as("user_id")
                )
                .distinct();
    }

    public Dataset<Row> transformEpics(Dataset<Row> rawEpics) {
        return rawEpics.select(
                col("id").as("original_id"),
                col("project").as("project_id"),
                col("subject").as("epic_name")
        );
    }

    public Dataset<Row> transformTags(Dataset<Row> rawTags) {
        return rawTags.select(
                        col("project").as("project_id"),
                        explode(col("tags")).as("tag") // Explodes the "tags" array into separate rows
                )
                .select(
                        col("tag").getItem(0).as("original_id"),
                        col("project_id"),
                        col("tag").getItem(0).as("tag_name")
                )
                .distinct();
    }

    public Dataset<Row> transformStatus(Dataset<Row> rawStatus) {
        return rawStatus
                .select(
                        col("status").as("original_id"),
                        lit(TOOL_ID).as("tool_id"),
                        col("project").as("project_id"),
                        col("status_extra_info.name").as("status_name")
                )
                .distinct();
    }

    public Dataset<Row> transformTasks(Dataset<Row> rawTasks) {
        return rawTasks
                .select(
                        col("id").as("original_id"),
                        col("subject").as("task_name"),
                        col("is_blocked").as("is_blocked"),
                        when(col("user_story").isNull(), true).otherwise(false).as("is_storyless"),
                        col("created_date").cast("date").as("created_at"),
                        col("finished_date").cast("date").as("completed_at"),
                        col("due_date").as("due_date"),
                        col("status").as("status_id"),
                        col("assigned_to").as("user_id"),
                        col("user_story_extra_info.id").as("story_id"),
                        lit(TOOL_ID).as("tool_id")
                );
    }
}
