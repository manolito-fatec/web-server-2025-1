package com.manolito.dashflow.dto.dw;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TaskProjectDto {
    private String projectName;
    private String projectId;
    private Integer count;
}