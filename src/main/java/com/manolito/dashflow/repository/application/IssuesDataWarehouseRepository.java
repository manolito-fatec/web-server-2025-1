package com.manolito.dashflow.repository.application;

import com.manolito.dashflow.dto.dw.IssueCountDto;
import com.manolito.dashflow.dto.dw.IssueFilterRequestDto;
import com.manolito.dashflow.enums.IssuePriority;
import com.manolito.dashflow.enums.IssueSeverity;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    public List<IssueCountDto> getIssueCountsByFilter(IssueFilterRequestDto filter) {
        // Base query without optional filters
        StringBuilder sql = new StringBuilder("""
        SELECT typ.type_name AS type, COUNT(fi.issue_id) AS count
        FROM dw_dashflow.fact_issues fi
        JOIN dw_dashflow.projects proj ON fi.project_id = proj.project_id
        JOIN dw_dashflow.issue_severity sev ON fi.severity_id = sev.severity_id
        JOIN dw_dashflow.issue_priority pri ON fi.priority_id = pri.priority_id
        JOIN dw_dashflow.issue_type typ ON fi.type_id = typ.type_id
        WHERE proj.original_id = :projectId
        AND sev.is_current = TRUE
        AND pri.is_current = TRUE
        AND typ.is_current = TRUE
        """);

        Map<String, Object> params = new HashMap<>();
        params.put("projectId", filter.getProjectId());

        // Handle severities List to append to the WHERE filter if not null
        if (filter.getSeverities() != null && !filter.getSeverities().isEmpty()) {
            sql.append(" AND sev.severity_name IN (:severities)");
            params.put("severities", filter.getSeverities().stream()
                    .map(IssueSeverity::getValue)
                    .collect(Collectors.toList()));
        }

        // Handle priorities List to append to the WHERE filter if not null
        if (filter.getPriorities() != null && !filter.getPriorities().isEmpty()) {
            sql.append(" AND pri.priority_name IN (:priorities)");
            params.put("priorities", filter.getPriorities().stream()
                    .map(IssuePriority::getValue)
                    .collect(Collectors.toList()));
        }

        // Finally adds the GROUP BY clause with or without the severities and priorities filters
        sql.append(" GROUP BY typ.type_name");

        return jdbcTemplate.query(
                sql.toString(),
                params,
                (rs, rowNum) -> IssueCountDto.builder()
                        .count(rs.getInt("count"))
                        .type(rs.getString("type"))
                        .build()
        );
    }
}
