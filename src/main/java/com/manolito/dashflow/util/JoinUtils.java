package com.manolito.dashflow.util;

import lombok.RequiredArgsConstructor;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.springframework.stereotype.Component;

import static org.apache.spark.sql.functions.col;

@Component
public class JoinUtils {

    /**
     * Joins the status dataset with projects dataset to map original project IDs to their corresponding database IDs.
     *
     * @param statusDF The dataset containing status information with original project IDs
     * @param projectsDF The dataset containing project information with original and new IDs
     * @return A new dataset with updated project IDs, containing original_id, status_name, and project_id columns
     */
    public Dataset<Row> joinStatusProject(Dataset<Row> statusDF, Dataset<Row> projectsDF) {
        Dataset<Row> joined = statusDF
                .join(projectsDF, statusDF.col("project_id").equalTo(projectsDF.col("original_id")));
        return joined.select(
                statusDF.col("original_id"),
                statusDF.col("status_name"),
                projectsDF.col("project_id").alias("project_id")
        );
    }

    /**
     * Joins the user dataset with projects dataset to map original project IDs to their corresponding database IDs.
     *
     * @param userDF The dataset containing user information with original project IDs
     * @param projectsDF The dataset containing project information with original and new IDs
     * @return A new dataset with updated project IDs, containing original_id, user_name, tool_id,
     *         and the mapped project_id columns
     */
    public Dataset<Row> joinUserProject(Dataset<Row> userDF, Dataset<Row> projectsDF) {
        Dataset<Row> joined = userDF
                .join(projectsDF, userDF.col("project_id").equalTo(projectsDF.col("original_id")));
        return joined.select(
                userDF.col("original_id"),
                userDF.col("user_name"),
                userDF.col("tool_id"),
                projectsDF.col("project_id")
        );
    }

    /**
     * Joins epics dataset with projects dataset to associate epics with their corresponding project IDs.
     *
     * @param epicsDF Dataset containing epic information with original project IDs
     * @param projectsDF Dataset containing project information with original and mapped IDs
     * @return Dataset containing epic original_id, mapped project_id, and epic_name
     */
    public Dataset<Row> joinEpicProject(Dataset<Row> epicsDF,
                                               Dataset<Row> projectsDF) {

        Dataset<Row> joinedDF = epicsDF
                .join(projectsDF, epicsDF.col("project_id").equalTo(projectsDF.col("original_id")));
        return joinedDF.select(
                epicsDF.col("original_id"),
                projectsDF.col("project_id"),
                epicsDF.col("epic_name")
        );
    }

    public Dataset<Row> joinIssueProject(Dataset<Row> issuesDF,
                                                Dataset<Row> projectsDF,
                                                String name) {
        Dataset<Row> joinedDF = issuesDF
                .join(projectsDF, issuesDF.col("project_id").equalTo(projectsDF.col("original_id")));
        return joinedDF.select(
                issuesDF.col("original_id"),
                projectsDF.col("project_id"),
                issuesDF.col(name)
        );
    }

    /**
     * Joins tags dataset with projects dataset to associate tags with their corresponding project IDs.
     *
     * @param tagsDF Dataset containing tag information with original project IDs
     * @param projectsDF Dataset containing project information with original and mapped IDs
     * @return Dataset containing tag original_id, mapped project_id, and tag_name
     */
    public Dataset<Row> joinTagProject(Dataset<Row> tagsDF,
                                              Dataset<Row> projectsDF) {

        Dataset<Row> joinedDF = tagsDF
                .join(projectsDF, tagsDF.col("project_id").equalTo(projectsDF.col("original_id")));
        return joinedDF.select(
                tagsDF.col("original_id"),
                projectsDF.col("project_id"),
                tagsDF.col("tag_name")
        );
    }

    /**
     * Joins stories dataset with projects and epics datasets to associate stories with their corresponding project and epic IDs.
     *
     * @param storiesDF Dataset containing story information with original project and epic IDs
     * @param projectsDF Dataset containing project information with original and mapped IDs
     * @param epicsDF Dataset containing epic information with original IDs
     * @return Dataset containing story original_id, mapped project_id, epic_id, story_name, and is_finished
     */
    public Dataset<Row> joinStoryProjectAndEpic(Dataset<Row> storiesDF,
                                                       Dataset<Row> projectsDF,
                                                       Dataset<Row> epicsDF) {
        Dataset<Row> joinedDF = storiesDF
                .join(projectsDF, storiesDF.col("project_id").equalTo(projectsDF.col("original_id")))
                .join(epicsDF, storiesDF.col("epic_id").equalTo(epicsDF.col("original_id")), "left");

        return joinedDF.select(
                storiesDF.col("original_id"),
                projectsDF.col("project_id"),
                storiesDF.col("epic_id").alias("epic_id"),
                storiesDF.col("story_name"),
                storiesDF.col("is_finished")
        );
    }

    /**
     * Joins task dataset with status, user, story and date datasets to create a consolidated fact table for tasks.
     *
     * @param tasksDF Dataset containing task information with original IDs
     * @param statusDF Dataset containing status information with original and mapped IDs
     * @param userDF Dataset containing user information with original and mapped IDs
     * @param storiesDF Dataset containing story information with original and mapped IDs
     * @param datesDF Dataset containing date dimension information
     * @return Dataset containing task original_id, status_id, assignee_id, tool_id, story_id, created_at,
     *         completed_at, due_date, task_name, is_blocked, and is_storyless
     */
    public Dataset<Row> joinFactTask(Dataset<Row> tasksDF,
                                            Dataset<Row> statusDF,
                                            Dataset<Row> userDF,
                                            Dataset<Row> storiesDF,
                                            Dataset<Row> datesDF) {

        Dataset<Row> joinedDF = tasksDF
                .join(statusDF, tasksDF.col("status_id").equalTo(statusDF.col("original_id")))
                .join(userDF, tasksDF.col("user_id").equalTo(userDF.col("original_id")))
                .join(storiesDF, tasksDF.col("story_id").equalTo(storiesDF.col("original_id")));

        joinedDF = mapDateColumn(joinedDF, datesDF, "created_at", "created_date_id");
        joinedDF = mapDateColumn(joinedDF, datesDF, "completed_at", "completed_date_id");
        joinedDF = mapDateColumn(joinedDF, datesDF, "due_date", "due_date_id");

        return joinedDF.select(
                tasksDF.col("original_id"),
                statusDF.col("status_id"),
                userDF.col("user_id").as("assignee_id"),
                tasksDF.col("tool_id"),
                storiesDF.col("story_id"),
                col("created_date_id").as("created_at"),
                col("completed_date_id").as("completed_at"),
                col("due_date_id").as("due_date"),
                tasksDF.col("task_name"),
                tasksDF.col("is_blocked"),
                tasksDF.col("is_storyless")
        );
    }

    /**
     * Joins issue dataset with status, user, project and date datasets to create a consolidated fact table for issues.
     *
     * @param issuesDF Dataset containing issue information with original IDs
     * @param statusDF Dataset containing status information with original and mapped IDs
     * @param userDF Dataset containing user information with original and mapped IDs
     * @param projectDF Dataset containing project information with original and mapped IDs
     * @param datesDF Dataset containing date dimension information
     * @return Dataset containing issue original_id, status_id, assignee_id, project_id, created_at,
     *         completed_at, and issue_name
     */
    public Dataset<Row> joinFactIssue(Dataset<Row> issuesDF,
                                             Dataset<Row> statusDF,
                                             Dataset<Row> userDF,
                                             Dataset<Row> projectDF,
                                             Dataset<Row> issueTypeDF,
                                             Dataset<Row> issueSeverityDF,
                                             Dataset<Row> issuePriorityDF,
                                             Dataset<Row> datesDF) {

        Dataset<Row> joinedDF = issuesDF
                .join(statusDF, issuesDF.col("status_id").equalTo(statusDF.col("original_id")))
                .join(userDF, issuesDF.col("user_id").equalTo(userDF.col("original_id")))
                .join(projectDF, issuesDF.col("project_id").equalTo(projectDF.col("original_id")))
                .join(issueTypeDF, issuesDF.col("type_id").equalTo(issueTypeDF.col("original_id")))
                .join(issuePriorityDF, issuesDF.col("priority_id").equalTo(issuePriorityDF.col("original_id")))
                .join(issueSeverityDF, issuesDF.col("severity_id").equalTo(issueSeverityDF.col("original_id")));

        joinedDF = mapDateColumn(joinedDF, datesDF, "created_at", "created_date_id");
        joinedDF = mapDateColumn(joinedDF, datesDF, "completed_at", "completed_date_id");

        return joinedDF.select(
                issuesDF.col("original_id"),
                statusDF.col("status_id"),
                userDF.col("user_id").as("assignee_id"),
                projectDF.col("project_id"),
                issueTypeDF.col("type_id"),
                issueSeverityDF.col("severity_id"),
                issuePriorityDF.col("priority_id"),
                col("created_date_id").as("created_at"),
                col("completed_date_id").as("completed_at"),
                issuesDF.col("issue_name")
        );
    }

    /**
     * Helper method that maps a date column to its corresponding date dimension key.
     * Performs a left join with the date dimension table and renames the resulting date_id column.
     *
     * @param df Input DataFrame containing the date column to be mapped
     * @param datesDF Date dimension DataFrame (must include: date_date, date_id)
     * @param dateColumn Name of the date column in the input DataFrame to be mapped
     * @param outputColumn Name to be given to the resulting date dimension key column
     * @return DataFrame with the date column mapped to its dimension key
     */
    private Dataset<Row> mapDateColumn(Dataset<Row> df, Dataset<Row> datesDF, String dateColumn, String outputColumn) {
        return df.join(datesDF,
                        df.col(dateColumn).cast("date").equalTo(datesDF.col("date_date")),
                        "left")
                .withColumnRenamed("date_id", outputColumn)
                .drop("date_date", "month", "year", "quarter", "day_of_week",
                        "day_of_month", "day_of_year", "is_weekend");
    }
}
