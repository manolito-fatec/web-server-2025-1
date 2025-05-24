package com.manolito.dashflow.service.application;

import com.manolito.dashflow.dto.dw.IssueCountDto;
import com.manolito.dashflow.dto.dw.IssueFilterRequestDto;
import com.manolito.dashflow.enums.IssuePriority;
import com.manolito.dashflow.enums.IssueSeverity;
import com.manolito.dashflow.repository.application.IssuesDataWarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class IssuesService {
    private final IssuesDataWarehouseRepository issuesDataWarehouseRepository;

    /**
     * Retrieves the count of issues grouped by type for a specific project with optional severity and priority filters.
     *
     * @param filter the filter criteria containing:
     *               <ul>
     *                 <li><b>projectId</b> - ID of the project to query issues for (required)</li>
     *                 <li><b>severities</b> - list of severity levels to filter by (optional, returns all if null)</li>
     *                 <li><b>priorities</b> - list of priority levels to filter by (optional, returns all if null)</li>
     *               </ul>
     * @return list of IssueCountDto objects containing issue type and count
     * @throws NoSuchElementException if no issues are found for the given criteria
     * @throws IllegalArgumentException if the filter or projectId is null
     */
    public List<IssueCountDto> getIssueCountsByFilter(IssueFilterRequestDto filter) {
        if (filter == null || filter.getProjectId() == null) {
            throw new IllegalArgumentException("Filter and project ID must not be null");
        }

        return issuesDataWarehouseRepository.getIssueCountsByFilter(filter);
    }
    
    /**
     * Retrieves all current issues for a specific project, grouped by issue type.
     *
     * @param projectId the ID of the project to query issues for
     * @return a list of IssueCount objects containing issue type and count
     * @throws NoSuchElementException if no issues are found for the given project
     */
    public List<IssueCountDto> getAllCurrentIssuesGroupedByType(String projectId) {
        List<IssueCountDto> issuesGrouped = issuesDataWarehouseRepository.getAllCurrentIssuesGroupedByType(projectId);

        if (issuesGrouped.isEmpty()) {
            throw new NoSuchElementException("No issues found for the given project");
        }

        return issuesGrouped;
    }
}
