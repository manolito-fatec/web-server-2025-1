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

}