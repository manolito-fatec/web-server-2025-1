package com.manolito.dashflow.repository.application;

import com.manolito.dashflow.dto.dw.StatusCountDto;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TasksDataWarehouseRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    public Optional<Integer> getTotalTasksByOperator(int userId) {
        String sql = "SELECT COUNT(ft.task_id) AS total_task_count " +
                "FROM dataflow_appl.users u " +
                "LEFT JOIN dataflow_appl.accounts acc " +
                "ON u.user_id = acc.user_id " +
                "LEFT JOIN dw_tasks.users tu " +
                "ON acc.account = tu.original_id " +
                "LEFT JOIN dw_tasks.fact_tasks ft " +
                "ON tu.user_id = ft.assignee_id " +
                "WHERE u.user_id = :userId GROUP BY u.user_id";

        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        try {
            Integer result = jdbcTemplate.queryForObject(sql, params, Integer.class);
            return Optional.ofNullable(result);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Optional<Integer> getTotalTasksByOperatorBetween(int userId, LocalDate startDate, LocalDate endDate) {
        Date start = Date.valueOf(startDate);
        Date end = Date.valueOf(endDate);


        String sql = "SELECT COUNT(ft.task_id) AS total_task_count " +
                "FROM dataflow_appl.users u " +
                "LEFT JOIN dataflow_appl.accounts acc ON u.user_id = acc.user_id " +
                "LEFT JOIN dw_tasks.users tu ON acc.account = tu.original_id " +
                "LEFT JOIN dw_tasks.fact_tasks ft ON tu.user_id = ft.assignee_id " +
                "LEFT JOIN dw_tasks.dates created_date ON ft.created_at = created_date.date_id " +
                "LEFT JOIN dw_tasks.dates completed_date ON ft.completed_at = completed_date.date_id " +
                "LEFT JOIN dw_tasks.dates due_date ON ft.due_date = due_date.date_id " +
                "WHERE u.user_id = :userId " +
                "AND created_date.date_date BETWEEN :start AND :end " +
                "AND completed_date.date_date BETWEEN :start AND :end " +
                "AND due_date.date_date BETWEEN :start AND :end " +
                "GROUP BY u.user_id";

        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("start", start);
        params.put("end", end);

        try {
            Integer result = jdbcTemplate.queryForObject(sql, params, Integer.class);
            return Optional.ofNullable(result);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public List<StatusCountDto> getTotalTasksByStatusByOperatorBetween(int userId, LocalDate startDate, LocalDate endDate) {
        Date start = Date.valueOf(startDate);
        Date end = Date.valueOf(endDate);

        String sql = "SELECT st.status_name, COUNT(ft.task_id) AS task_count " +
                "FROM dataflow_appl.users u " +
                "LEFT JOIN dataflow_appl.accounts acc ON u.user_id = acc.user_id " +
                "LEFT JOIN dw_tasks.users tu ON acc.account = tu.original_id " +
                "LEFT JOIN dw_tasks.fact_tasks ft ON tu.user_id = ft.assignee_id " +
                "LEFT JOIN dw_tasks.dates created_date ON ft.created_at = created_date.date_id " +
                "LEFT JOIN dw_tasks.dates completed_date ON ft.completed_at = completed_date.date_id " +
                "LEFT JOIN dw_tasks.dates due_date ON ft.due_date = due_date.date_id " +
                "LEFT JOIN dw_tasks.status st " +
                "ON ft.status_id = st.status_id " +
                "WHERE u.user_id = :userId " +
                "AND st.is_current = TRUE " +
                "AND created_date.date_date BETWEEN :start AND :end " +
                "AND completed_date.date_date BETWEEN :start AND :end " +
                "AND due_date.date_date BETWEEN :start AND :end " +
                "GROUP BY u.user_id, st.status_name";

        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("start", start);
        params.put("end", end);
        return jdbcTemplate.query(
                sql,
                params,
                (rs, rowNum) -> new StatusCountDto(
                        rs.getString("status_name"),
                        rs.getInt("task_count")
                ));
    }

    public Optional<Integer> getTotalProjectsByUserId(int userId) {
        String sql = "SELECT COUNT DISTINCT(prj.original_id) AS total_project_count " +
                "FROM dataflow_appl.users u " +
                "LEFT JOIN dataflow_appl.accounts acc " +
                "ON u.user_id = acc.user_id " +
                "LEFT JOIN dw_tasks.users tu " +
                "ON acc.account = tu.original_id " +
                "LEFT JOIN dw_tasks.fact_tasks ft " +
                "ON tu.user_id = ft.assignee_id " +
                "LEFT JOIN dw_tasks.stories st " +
                "ON ft.story_id = st.story_id " +
                "LEFT JOIN dw_tasks.epics ep " +
                "ON st.epic_id = ep.epic_id " +
                "LEFT JOIN dw_tasks.projects prj " +
                "ON ep.project_id = prj.project_id" +
                "WHERE u.user_id = :userId GROUP BY u.user_id";

        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        try {
            Integer result = jdbcTemplate.queryForObject(sql, params, Integer.class);
            return Optional.ofNullable(result);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public List<StatusCountDto> getTaskCountGroupByStatusByUserIdAndProjectId(int userId, int projectId) {
        String sql = "SELECT st.status_name, COUNT(ft.task_id) as task_count " +
                "FROM dataflow_appl.users u " +
                "LEFT JOIN dataflow_appl.accounts acc " +
                "ON u.user_id = acc.user_id " +
                "LEFT JOIN dw_tasks.users tu " +
                "ON acc.account = tu.original_id " +
                "LEFT JOIN dw_tasks.fact_tasks ft " +
                "ON tu.user_id = ft.assignee_id " +
                "LEFT JOIN dw_tasks.status st " +
                "ON ft.status_id = st.status_id " +
                "LEFT JOIN dw_tasks.projects prj " +
                "ON st.project_id = prj.project_id " +
                "WHERE u.user_id = :userId " +
                "AND prj.original_id = :projectId " +
                "AND st.is_current = TRUE " +
                "AND tu.is_current = TRUE " +
                "GROUP BY st.status_name";

        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("projectId", String.valueOf(projectId));

        return jdbcTemplate.query(
                sql,
                params,
                (rs, rowNum) -> new StatusCountDto(
                        rs.getString("status_name"),
                        rs.getInt("task_count")
                )
        );
    }
}
