package com.manolito.dashflow.service.application;

import com.manolito.dashflow.dto.dw.IssueCountDto;
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
     * Retrieves the count of issues for a specific project, severity, and priority, grouped by issue type.
     *
     * @param projectId the ID of the project to query issues for
     * @param severity the severity level of the issues to filter
     * @param priority the priority level of the issues to filter
     * @return a list of IssueCount objects containing issue type and count
     * @throws NoSuchElementException if no issues are found for the given criteria
     */
    public List<IssueCountDto> getIssueCountsByProjectSeverityAndPriority(
            String projectId, IssueSeverity severity, IssuePriority priority) {

        List<IssueCountDto> result = new ArrayList<>();

        result.add(new IssueCountDto("Bug",
                issuesDataWarehouseRepository.getIssueCountByType(projectId, severity.getValue(), priority.getValue(), "Bug").orElse(0)));
        result.add(new IssueCountDto("Enhancement",
                issuesDataWarehouseRepository.getIssueCountByType(projectId, severity.getValue(), priority.getValue(), "Enhancement").orElse(0)));
        result.add(new IssueCountDto("Question",
                issuesDataWarehouseRepository.getIssueCountByType(projectId, severity.getValue(), priority.getValue(), "Question").orElse(0)));

        return result;
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
