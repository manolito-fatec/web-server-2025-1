package com.manolito.dashflow.repository.application;

import com.manolito.dashflow.dto.dw.CreatedDoneDto;
import com.manolito.dashflow.dto.dw.StatusCountDto;
import com.manolito.dashflow.dto.dw.TaskOperatorDto;
import com.manolito.dashflow.dto.dw.TaskTagDto;
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
                "FROM dashflow_appl.users u " +
                "LEFT JOIN dashflow_appl.accounts acc " +
                "ON u.user_id = acc.user_id " +
                "LEFT JOIN dw_dashflow.users tu " +
                "ON acc.account = tu.original_id " +
                "LEFT JOIN dw_dashflow.fact_tasks ft " +
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
                "FROM dashflow_appl.users u " +
                "LEFT JOIN dashflow_appl.accounts acc ON u.user_id = acc.user_id " +
                "LEFT JOIN dw_dashflow.users tu ON acc.account = tu.original_id " +
                "LEFT JOIN dw_dashflow.fact_tasks ft ON tu.user_id = ft.assignee_id " +
                "LEFT JOIN dw_dashflow.dates created_date ON ft.created_at = created_date.date_id " +
                "LEFT JOIN dw_dashflow.dates completed_date ON ft.completed_at = completed_date.date_id " +
                "LEFT JOIN dw_dashflow.dates due_date ON ft.due_date = due_date.date_id " +
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

    public List<CreatedDoneDto> getTotalTasksByStatusByOperatorBetween(int userId, LocalDate startDate, LocalDate endDate) {
        Date start = Date.valueOf(startDate);
        Date end = Date.valueOf(endDate);

        String sql = "SELECT COUNT(DISTINCT CASE WHEN created_date.date_date BETWEEN :start AND :end THEN ft.task_id END) AS created_task_count," +
        "COUNT(DISTINCT CASE WHEN completed_date.date_date BETWEEN :start AND :end THEN ft.task_id END) AS completed_task_count " +
        "FROM dashflow_appl.users u " +
        "LEFT JOIN dashflow_appl.accounts acc ON u.user_id = acc.user_id "  +
        "LEFT JOIN dw_dashflow.users tu ON acc.account = tu.original_id " +
        "LEFT JOIN dw_dashflow.fact_tasks ft ON tu.user_id = ft.assignee_id " +
        "LEFT JOIN dw_dashflow.dates created_date ON ft.created_at = created_date.date_id " +
        "LEFT JOIN dw_dashflow.dates completed_date ON ft.completed_at = completed_date.date_id " +
        "WHERE u.user_id = 1 " +
        "GROUP BY u.user_id;";

        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("start", start);
        params.put("end", end);
        return jdbcTemplate.query(
                sql,
                params,
                (rs, rowNum) -> new CreatedDoneDto(
                        rs.getInt("created_task_count"),
                        rs.getInt("completed_task_count")
                ));
    }

    public Optional<CreatedDoneDto> getAllCreatedAndCompletedTasksByProjectBetween(String projectId, LocalDate startDate, LocalDate endDate) {
        Date start = Date.valueOf(startDate);
        Date end = Date.valueOf(endDate);

        String sql = "SELECT COUNT(DISTINCT CASE WHEN created_date.date_date BETWEEN :start AND :end THEN ft.task_id END) AS created_task_count, " +
                "COUNT(DISTINCT CASE WHEN completed_date.date_date BETWEEN :start AND :end THEN ft.task_id END) AS completed_task_count " +
                "FROM dw_dashflow.projects prj " +
                "LEFT JOIN dw_dashflow.epics ep ON prj.project_id = ep.project_id " +
                "LEFT JOIN dw_dashflow.stories st ON ep.epic_id = st.epic_id " +
                "LEFT JOIN dw_dashflow.fact_tasks ft ON st.story_id = ft.story_id " +
                "LEFT JOIN dw_dashflow.dates created_date ON ft.created_at = created_date.date_id  " +
                "LEFT JOIN dw_dashflow.dates completed_date ON ft.completed_at = completed_date.date_id " +
                "WHERE prj.original_id = :projectId";

        Map<String, Object> params = new HashMap<>();
        params.put("projectId", projectId);
        params.put("start", start);
        params.put("end", end);

        try {
            CreatedDoneDto result = jdbcTemplate.queryForObject(
                    sql,
                    params,
                    (rs, rowNum) -> CreatedDoneDto.builder()
                            .createdTaskCount(rs.getInt("created_task_count"))
                            .completedTaskCount(rs.getInt("completed_task_count"))
                            .build()
            );
            return Optional.ofNullable(result);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Optional<Integer> getTotalProjectsByUserId(int userId) {
        String sql = "SELECT COUNT (DISTINCT prj.original_id) AS total_project_count " +
                "FROM dashflow_appl.users u " +
                "LEFT JOIN dashflow_appl.accounts acc " +
                "ON u.user_id = acc.user_id " +
                "LEFT JOIN dw_dashflow.users tu " +
                "ON acc.account = tu.original_id " +
                "LEFT JOIN dw_dashflow.fact_tasks ft " +
                "ON tu.user_id = ft.assignee_id " +
                "LEFT JOIN dw_dashflow.stories st " +
                "ON ft.story_id = st.story_id " +
                "LEFT JOIN dw_dashflow.epics ep " +
                "ON st.epic_id = ep.epic_id " +
                "LEFT JOIN dw_dashflow.projects prj " +
                "ON ep.project_id = prj.project_id " +
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

    public List<StatusCountDto> getTaskCountGroupByStatusByUserIdAndProjectId(int userId, String projectId) {
        String sql = "SELECT st.status_name, COUNT(ft.task_id) as task_count " +
                "FROM dashflow_appl.users u " +
                "LEFT JOIN dashflow_appl.accounts acc " +
                "ON u.user_id = acc.user_id " +
                "LEFT JOIN dw_dashflow.users tu " +
                "ON acc.account = tu.original_id " +
                "LEFT JOIN dw_dashflow.fact_tasks ft " +
                "ON tu.user_id = ft.assignee_id " +
                "LEFT JOIN dw_dashflow.status st " +
                "ON ft.status_id = st.status_id " +
                "LEFT JOIN dw_dashflow.projects prj " +
                "ON st.project_id = prj.project_id " +
                "WHERE u.user_id = :userId " +
                "AND prj.original_id = :projectId " +
                "AND st.is_current = TRUE " +
                "AND tu.is_current = TRUE " +
                "GROUP BY st.status_name";

        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        params.put("projectId", projectId);

        return jdbcTemplate.query(
                sql,
                params,
                (rs, rowNum) -> new StatusCountDto(
                        rs.getString("status_name"),
                        rs.getInt("task_count")
                )
        );
    }

    public List<StatusCountDto> getTaskCountGroupByStatusByProjectId(String projectId) {
        String sql = "SELECT st.status_name, COUNT(ft.task_id) as task_count " +
                "FROM dw_dashflow.projects prj " +
                "LEFT JOIN dw_dashflow.epics ep ON prj.project_id = ep.project_id " +
                "LEFT JOIN dw_dashflow.stories sto ON ep.epic_id = sto.epic_id " +
                "LEFT JOIN dw_dashflow.fact_tasks ft ON sto.story_id = ft.story_id " +
                "LEFT JOIN dw_dashflow.status st " +
                "ON ft.status_id = st.status_id " +
                "WHERE prj.original_id = :projectId " +
                "AND st.is_current = TRUE " +
                "GROUP BY st.status_name";

        Map<String, Object> params = new HashMap<>();
        params.put("projectId", projectId);

        return jdbcTemplate.query(
                sql,
                params,
                (rs, rowNum) -> new StatusCountDto(
                        rs.getString("status_name"),
                        rs.getInt("task_count")
                )
        );
    }

    public Optional<Double> getAverageTimeCard(Integer userId) {
        String sql = "SELECT ROUND((AVG(completed.date_date - created.date_date)/3),2) AS average_time " +
                "FROM dw_dashflow.fact_tasks ft " +
                "JOIN dw_dashflow.dates created ON ft.created_at = created.date_id " +
                "JOIN dw_dashflow.dates completed ON ft.completed_at = completed.date_id " +
                "JOIN dw_dashflow.users u ON assignee_id  = u.user_id " +
                "WHERE ft.completed_at IS NOT NULL " +
                "AND u.user_id = :userId";

        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        try {
            Double result = jdbcTemplate.queryForObject(sql, params, Double.class);
            return Optional.ofNullable(result);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Optional<Double> getAverageTimeCardByProjectId(String projectId) {
        String sql = "SELECT ROUND((AVG(completed.date_date - created.date_date)/3),2) AS average_time " +
                "FROM dw_dashflow.fact_tasks ft " +
                "JOIN dw_dashflow.dates created ON ft.created_at = created.date_id " +
                "JOIN dw_dashflow.dates completed ON ft.completed_at = completed.date_id " +
                "JOIN dw_dashflow.stories st ON ft.story_id = ft.story_id " +
                "JOIN dw_dashflow.epics ep ON ep.epic_id = st.epic_id " +
                "JOIN dw_dashflow.projects prj ON prj.project_id = ep.project_id " +
                "WHERE ft.completed_at IS NOT NULL " +
                "AND prj.original_id = :projectId";

        Map<String, Object> params = new HashMap<>();
        params.put("projectId", projectId);
        try {
            Double result = jdbcTemplate.queryForObject(sql, params, Double.class);
            return Optional.ofNullable(result);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Optional<Integer> getTotalCardsForManager(int userId) {
        String sql = "SELECT COUNT(ft.task_id) AS total_cards " +
                "FROM dataflow_appl.users u " +
                "LEFT JOIN dataflow_appl.accounts acc ON u.user_id = acc.user_id " +
                "LEFT JOIN dw_dashflow.users tu ON acc.account = tu.original_id " +
                "LEFT JOIN dw_dashflow.fact_tasks ft ON tu.user_id = ft.assignee_id " +
                "LEFT JOIN dw_dashflow.status st ON ft.status_id = st.status_id " +
                "LEFT JOIN dw_dashflow.projects prj ON st.project_id = prj.project_id " +
                "WHERE u.user_id = :userId " +
                "AND prj.is_current = TRUE " +
                "AND st.is_current = TRUE " +
                "AND tu.is_current = TRUE";

        Map<String, Object> params = new HashMap<>();
        params.put("userId", userId);

        try {
            Integer result = jdbcTemplate.queryForObject(sql, params, Integer.class);
            return Optional.ofNullable(result);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public List<TaskTagDto> getTaskCountGroupByTagByProjectId(String projectId) {
        String sql = "SELECT tag.tag_name, COUNT(ft.task_id) as task_count " +
                "FROM dw_dashflow.projects prj " +
                "LEFT JOIN dw_dashflow.epics ep ON prj.project_id = ep.project_id " +
                "LEFT JOIN dw_dashflow.stories sto ON ep.epic_id = sto.epic_id " +
                "LEFT JOIN dw_dashflow.fact_tasks ft ON sto.story_id = ft.story_id " +
                "LEFT JOIN dw_dashflow.status st " +
                "ON ft.status_id = st.status_id " +
                "LEFT JOIN dw_dashflow.task_tag tt " +
                "ON tt.task_id = ft.task_id " +
                "LEFT JOIN dw_dashflow.tags tag " +
                "ON tt.tag_id = tag.tag_id "+
                "WHERE prj.original_id = :projectId " +
                "AND tag.is_current = TRUE " +
                "GROUP BY tag.tag_name";

        Map<String, Object> params = new HashMap<>();
        params.put("projectId", projectId);

        return jdbcTemplate.query(
                sql,
                params,
                (rs, rowNum) -> new TaskTagDto(
                        rs.getString("tag_name"),
                        rs.getInt("task_count")
                )
        );
    }

    public List<TaskOperatorDto> getTaskCountGroupByOperatorByProjectId(String projectId) {
        String sql = """
                SELECT
                    us.user_name,
                    us.user_id,
                    COUNT(ft.task_id) AS total_cards
                FROM dw_dashflow.projects prj
                LEFT JOIN dw_dashflow.epics ep ON prj.project_id = ep.project_id
                LEFT JOIN dw_dashflow.stories sto ON ep.epic_id = sto.epic_id
                LEFT JOIN dw_dashflow.fact_tasks ft ON sto.story_id = ft.story_id
                LEFT JOIN dw_dashflow.users us ON ft.assignee_id = us.user_id
                WHERE prj.original_id = :projectId
                AND prj.is_current = TRUE
                AND ft.is_current = TRUE
                GROUP BY ft.task_id
                """;

        Map<String, Object> params = new HashMap<>();
        params.put("projectId", projectId);

        return jdbcTemplate.query(
                sql,
                params,
                (rs, rowNum) -> new TaskOperatorDto(
                        rs.getString("user_name"),
                        rs.getInt("user_id"),
                        rs.getInt("total_cards")
                )
        );
    }

    public Optional<Integer> getTaskReworksByProjectId(String projectId) {
        String sql = """
                SELECT
                    COUNT(ft.task_id) AS total
                FROM dw_dashflow.projects prj
                LEFT JOIN dw_dashflow.epics ep ON prj.project_id = ep.project_id
                LEFT JOIN dw_dashflow.stories sto ON ep.epic_id = sto.epic_id
                LEFT JOIN dw_dashflow.fact_tasks ft ON sto.story_id = ft.story_id
                LEFT JOIN dw_dashflow.users us ON ft.assignee_id = us.user_id
                WHERE prj.original_id = :projectId
                AND prj.is_current = TRUE
                AND ft.is_current = TRUE
                AND ft.completed_at IS NULL
                AND EXISTS (
                    SELECT 1 FROM dw_dashflow.fact_tasks ft_hist
                    WHERE ft_hist.original_id = ft.original_id
                    AND ft_hist.completed_at IS NOT NULL
                    AND ft_hist.is_current = FALSE
                )
                """;


        Map<String, Object> params = new HashMap<>();
        params.put("projectId", projectId);

        try {
            Integer result = jdbcTemplate.queryForObject(sql, params, Integer.class);
            return Optional.ofNullable(result);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}
