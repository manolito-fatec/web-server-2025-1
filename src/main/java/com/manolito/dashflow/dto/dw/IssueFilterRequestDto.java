package com.manolito.dashflow.dto.dw;

import com.manolito.dashflow.enums.IssuePriority;
import com.manolito.dashflow.enums.IssueSeverity;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class IssueFilterRequestDto {
    private String projectId;
    private List<IssueSeverity> severities;
    private List<IssuePriority> priorities;
}
