package com.manolito.dashflow.repository.application;

import java.util.ArrayList;
import java.util.List;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.manolito.dashflow.dto.application.ExportCsvAdminDto;
import com.manolito.dashflow.dto.application.ExportCsvManagerDto;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ExportCsvRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public List<ExportCsvManagerDto> getAllCurrentOperatorProjectAndQuantityOfCard ()
    {
        String sql = """
                 SELECT 
                   u.user_name,
                   p.project_name,
                   COUNT(ft.task_id) AS total_tasks
                 FROM 
                   api5.dashflow_appl.accounts a
                 JOIN 
                   api5.dw_dashflow.projects p ON a.project = p.original_id
                 JOIN 
                   api5.dw_dashflow.users u ON a.account = u.original_id
                 JOIN 
                   api5.dashflow_appl.roles r ON a.role_id = r.role_id
                 LEFT JOIN 
                   api5.dw_dashflow.fact_tasks ft ON ft.assignee_id = u.user_id
                 WHERE 
                   u.is_current = 'TRUE'
                   AND p.is_current = 'TRUE'
                 GROUP BY 
                   u.user_name, p.project_name;
                 """;

        List<ExportCsvManagerDto> result = new ArrayList<>();
        jdbcTemplate.query(sql, rs ->
        {
            result.add(new ExportCsvManagerDto(rs.getString("user_name"), rs.getString("project_name"),
                    rs.getInt("total_tasks")));
        });

        return result;
    }

    public List<ExportCsvAdminDto> getAllCurrentManagerAndProjectAndQuantityOfOperatorsAndQuantityOfCard ()
    {
        String sql = """
                    SELECT 
                         p.project_name,
                         u.user_name,
                         COUNT(DISTINCT u.user_name) AS total_operators,
                         COUNT(ft.task_id) AS total_tasks
                    FROM 
                         api5.dashflow_appl.accounts a
                    JOIN 
                    api5.dw_dashflow.projects p ON a.project = p.original_id
                    JOIN 
                         api5.dw_dashflow.users u ON a.account = u.original_id
                    JOIN 
                         api5.dashflow_appl.roles r ON a.role_id = r.role_id AND r.role_name = 'ROLE_MANAGER'
                    LEFT JOIN 
                         api5.dw_dashflow.fact_tasks ft ON ft.assignee_id = u.user_id
                    WHERE 
                         u.is_current = 'TRUE'
                    AND p.is_current = 'TRUE'
                    GROUP BY 
                       p.project_name, u.user_name;
                   """;

        List<ExportCsvAdminDto> result = new ArrayList<>();
        jdbcTemplate.query(sql, rs ->
        {
            result.add(new ExportCsvAdminDto(rs.getString("project_name"), rs.getString("user_name"),
                    rs.getInt("total_operators"), rs.getInt("total_tasks")));
        });

        return result;
    }
}
