package com.manolito.dashflow.repository.application;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.time.LocalDate;

@Repository
@RequiredArgsConstructor
public class TasksDataWarehouseRepository {
    private final JdbcTemplate jdbcTemplate;

    public Integer getTotalTasksByOperator(int userId) {
        String sql = "SELECT COUNT(ft.task_id) AS total_task_count " +
                "FROM dataflow_appl.users u " +
                "LEFT JOIN dataflow_appl.accounts acc " +
                "ON u.user_id = acc.user_id " +
                "LEFT JOIN dw_tasks.users tu " +
                "ON acc.account = tu.original_id " +
                "LEFT JOIN dw_tasks.fact_tasks ft " +
                "ON tu.user_id = ft.assignee_id " +
                "WHERE u.user_id = ? GROUP BY u.user_id";
        return jdbcTemplate.queryForObject(sql, Integer.class, userId);
    }

    public Integer getTotalTasksByOperatorBetween(int userId, LocalDate startDate, LocalDate endDate) {
        Date start = Date.valueOf(startDate);
        Date end = Date.valueOf(endDate);

        String sql = "SELECT COUNT(ft.task_id) AS total_task_count " +
                "FROM dataflow_appl.users u " +
                "LEFT JOIN dataflow_appl.accounts acc " +
                "ON u.user_id = acc.user_id " +
                "LEFT JOIN dw_tasks.users tu " +
                "ON acc.account = tu.original_id " +
                "LEFT JOIN dw_tasks.fact_tasks ft " +
                "ON tu.user_id = ft.assignee_id " +
                "WHERE u.user_id = ? GROUP BY u.user_id";
        return jdbcTemplate.queryForObject(sql, Integer.class, userId, start, end);
    }


}
