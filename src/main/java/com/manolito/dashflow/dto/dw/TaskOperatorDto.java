package com.manolito.dashflow.dto.dw;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TaskOperatorDto {
    private String operatorName;
    private Integer operatorId;
    private Integer count;
}
