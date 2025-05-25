package com.manolito.dashflow.dto.dw;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProjectTableDto {
    private String projectId;
    private String projectName;
    private String managerName;
    private Integer operatorCount;
    private Integer toolId;
}
