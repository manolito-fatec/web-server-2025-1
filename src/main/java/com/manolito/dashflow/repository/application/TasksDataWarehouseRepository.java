package com.manolito.dashflow.repository.application;

import com.manolito.dashflow.dto.dw.*;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.time.LocalDate;
import java.util.*;

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
                "FROM dashflow_appl.users u " +
                "LEFT JOIN dashflow_appl.accounts acc ON u.user_id = acc.user_id " +
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

    public Optional<Integer> getProjectCount() {
        String sql = """
                SELECT
                    COUNT(prj.original_id)
                FROM dw_dashflow.projects prj
                WHERE prj.is_current = TRUE
                """;
        try {
            Integer result = jdbcTemplate.queryForObject(sql, new HashMap<>(), Integer.class);
            return Optional.ofNullable(result);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public List<TaskProjectDto> getTaskCountGroupByProject() {
        String sql = """
                SELECT
                    COUNT(ft.task_id) AS total_cards,
                    prj.original_id,
                    prj.project_name
                FROM dw_dashflow.fact_tasks ft
                LEFT JOIN dw_dashflow.status st ON ft.status_id = st.status_id
                LEFT JOIN dw_dashflow.projects prj ON st.project_id = prj.project_id
                WHERE prj.is_current = TRUE
                AND st.is_current = TRUE
                GROUP BY prj.project_name, prj.original_id
                """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> new TaskProjectDto(
                        rs.getString("project_name"),
                        rs.getString("original_id"),
                        rs.getInt("total_cards")
                )
        );
    }

    public List<ProjectDto> getProjectsByTool(Integer toolId) {
        String sql = """
                SELECT
                    prj.original_id,
                    prj.project_name
                FROM dw_dashflow.projects prj
                LEFT JOIN dw_dashflow.tools too ON prj.tool_id = too.tool_id
                WHERE prj.is_current = TRUE
                AND too.is_current = TRUE
                AND too.tool_id = :toolId
                """;

        Map<String, Object> params = new HashMap<>();
        params.put("toolId", toolId);

        return jdbcTemplate.query(
                sql,
                params,
                (rs, rowNum) -> new ProjectDto(
                        rs.getString("user_name"),
                        rs.getString("user_id")
                )
        );
    }

    public List<UserDto> getUsersByProjectId(String projectId) {
        String sql = """
                SELECT
                    us.original_id,
                    us.user_name
                FROM dw_dashflow.users us
                LEFT JOIN dw_dashflow.tools too
                    ON us.tool_id = too.tool_id
                LEFT JOIN dw_dashflow.projects prj
                    ON too.tool_id = prj.tool_id
                WHERE us.is_current = TRUE
                AND too.is_current = TRUE
                AND prj.is_current = TRUE
                AND prj.original_id = :projectId
                """;

        Map<String, Object> params = new HashMap<>();
        params.put("projectId", projectId);

        return jdbcTemplate.query(
                sql,
                params,
                (rs, rowNum) -> new UserDto(
                        rs.getString("original_id"),
                        rs.getString("user_name")
                )
        );
    }

    public List<UserTableDto> getUsersPaginated(int page, int pageSize) {
        String sql = """
                 SELECT
                     appu.user_id AS user_id,
                     appu.username AS user_name,
                     appr.role_name AS user_role,
                     appu.email as user_email,
                     appt.tool_name,
                     appt.tool_id,
                     dwp.original_id AS project_id,
                     dwp.project_name,
                     appu.created_at AS created_at
                 FROM dashflow_appl.users appu
                 LEFT JOIN dashflow_appl.user_roles approle ON appu.user_id = approle.user_id
                 LEFT JOIN dashflow_appl.roles appr ON approle.role_id = appr.role_id
                 LEFT JOIN dashflow_appl.accounts appa ON appu.user_id = appa.user_id
                 LEFT JOIN dashflow_appl.tools appt ON appa.tool_id = appt.tool_id
                 LEFT JOIN dw_dashflow.users dwu ON appa.account = dwu.original_id AND dwu.is_current = TRUE
                 LEFT JOIN dw_dashflow.fact_tasks dwft ON dwu.user_id = dwft.assignee_id
                 LEFT JOIN dw_dashflow.stories dws ON dwft.story_id = dws.story_id AND dws.is_current = TRUE
                 LEFT JOIN dw_dashflow.epics dwe ON dws.epic_id = dwe.epic_id AND dwe.is_current = TRUE
                 LEFT JOIN dw_dashflow.projects dwp ON dwe.project_id = dwp.project_id AND dwp.is_current = TRUE
                 WHERE appr.role_id <> 3 -- SKIP LISTING ADMINS
                 GROUP BY
                    appu.user_id, appu.username, appr.role_name, appt.tool_name,
                    appt.tool_id, dwp.original_id, dwp.project_name, appu.created_at
                 ORDER BY appu.username ASC
                 LIMIT :limit OFFSET :offset
                 """;

        Map<String, Object> params = new HashMap<>();
        params.put("limit", pageSize);
        params.put("offset", (page - 1) * pageSize);

        return jdbcTemplate.query(
                sql,
                params,
                (rs, rowNum) -> UserTableDto.builder()
                        .userId(String.valueOf(rs.getInt("user_id")))
                        .userName(rs.getString("user_name"))
                        .userRole(rs.getString("user_role"))
                        .userEmail(rs.getString("user_email"))
                        .toolName(rs.getString("tool_name"))
                        .toolId(rs.getObject("tool_id", Integer.class))
                        .projectId(rs.getString("project_id"))
                        .projectName(rs.getString("project_name"))
                        .createdAt(rs.getTimestamp("created_at") != null ?
                                rs.getTimestamp("created_at").toLocalDateTime().toLocalDate() :
                                null)
                        .build()
        );
    }

    public int countAllApplicationUsers() {
        String sql = "SELECT COUNT(*) FROM dashflow_appl.users";

        Integer count = jdbcTemplate.getJdbcOperations().queryForObject(sql, Integer.class);
        return count != null ? count : 0;
    }
}
