package com.manolito.dashflow.dto.dw;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TaskOperatorDto {
    private String OperatorName;
    private Integer OperatorId;
    private Integer count;
}
