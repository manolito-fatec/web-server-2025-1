package com.manolito.dashflow.repository.application;

import com.manolito.dashflow.dto.dw.IssueCountDto;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class IssuesDataWarehouseRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    public List<IssueCountDto> getAllCurrentIssuesGroupedByType(String projectOriginalId) {
        String sql = """
                SELECT typ.type_name, COUNT(fi.issue_id) AS issue_count
                FROM dw_dashflow.fact_issues fi
                JOIN dw_dashflow.projects proj ON fi.project_id = proj.project_id
                JOIN dw_dashflow.issue_severity sev ON fi.severity_id = sev.severity_id
                JOIN dw_dashflow.issue_priority pri ON fi.priority_id = pri.priority_id
                JOIN dw_dashflow.issue_type typ ON fi.type_id = typ.type_id
                WHERE proj.original_id = :projectOriginalId
                AND sev.is_current = TRUE
                AND pri.is_current = TRUE
                AND typ.is_current = TRUE
                GROUP BY typ.type_name
                """;

        Map<String, Object> params = new HashMap<>();
        params.put("projectOriginalId", projectOriginalId);

        List<IssueCountDto> result = new ArrayList<>();
        jdbcTemplate.query(sql, params, rs -> {
            result.add(new IssueCountDto(
                    rs.getString("type_name"),
                    rs.getInt("issue_count")
            ));
        });

        return result;
    }

    public Optional<Integer> getIssueCountByType(String projectOriginalId, String severity, String priority, String type) {
        String sql = """
                SELECT COUNT(fi.issue_id) AS issue_count
                FROM dw_dashflow.fact_issues fi
                JOIN dw_dashflow.projects proj ON fi.project_id = proj.project_id
                JOIN dw_dashflow.issue_severity sev ON fi.severity_id = sev.severity_id
                JOIN dw_dashflow.issue_priority pri ON fi.priority_id = pri.priority_id
                JOIN dw_dashflow.issue_type typ ON fi.type_id = typ.type_id
                WHERE proj.original_id = :projectOriginalId
                AND sev.severity_name = :severity
                AND pri.priority_name = :priority
                AND typ.type_name = :type
                AND sev.is_current = TRUE
                AND pri.is_current = TRUE
                AND typ.is_current = TRUE
                """;

        Map<String, Object> params = new HashMap<>();
        params.put("projectOriginalId", projectOriginalId);
        params.put("severity", severity);
        params.put("priority", priority);
        params.put("type", type);

        try {
            Integer result = jdbcTemplate.queryForObject(sql, params, Integer.class);
            return Optional.ofNullable(result);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}
