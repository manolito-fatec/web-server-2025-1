package com.manolito.dashflow.service.application;

import com.manolito.dashflow.enums.IssuePriority;
import com.manolito.dashflow.enums.IssueSeverity;
import com.manolito.dashflow.repository.application.IssuesDataWarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class IssuesService {
    private final IssuesDataWarehouseRepository issuesDataWarehouseRepository;

    /**
     * Retrieves the count of issues for a specific project, severity, and priority, grouped by issue type.
     *
     * @param projectId the ID of the project to query issues for
     * @param severity the severity level of the issues to filter
     * @param priority the priority level of the issues to filter
     * @return a map where the key is the issue type ("Bug", "Enhancement", "Question") and the value is the count
     * @throws NoSuchElementException if no issues are found for the given criteria
     */
    public Map<String, Integer> getIssueCountsByProjectSeverityAndPriority(
            int projectId, IssueSeverity severity, IssuePriority priority) {

        Map<String, Integer> result = new HashMap<>();

        result.put("Bug", issuesDataWarehouseRepository.getIssueCountByType(projectId, severity.getValue(), priority.getValue(), "Bug").orElse(0));
        result.put("Enhancement", issuesDataWarehouseRepository.getIssueCountByType(projectId, severity.getValue(), priority.getValue(), "Enhancement").orElse(0));
        result.put("Question", issuesDataWarehouseRepository.getIssueCountByType(projectId, severity.getValue(), priority.getValue(), "Question").orElse(0));

        if (result.values().stream().allMatch(count -> count == 0)) {
            throw new NoSuchElementException("No issues found for the given criteria");
        }

        return result;
    }
    
    /**
     * Retrieves all current issues for a specific project, grouped by issue type.
     *
     * @param projectId the ID of the project to query issues for
     * @return a map where the key is the issue type and the value is the count of issues
     * @throws NoSuchElementException if no issues are found for the given project
     */
    public Map<String, Integer> getAllCurrentIssuesGroupedByType(int projectId) {
        Map<String, Integer> issuesGrouped = issuesDataWarehouseRepository.getAllCurrentIssuesGroupedByType(projectId);

        if (issuesGrouped.isEmpty()) {
            throw new NoSuchElementException("No issues found for the given project");
        }

        return issuesGrouped;
    }
}
