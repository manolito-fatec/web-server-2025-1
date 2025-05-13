package com.manolito.dashflow.dto.dw;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProjectTableDto {
    private String projectName;
    private String projectId;
    private String manager;
    private String operatorCount;
}
