package com.manolito.dashflow.dto.dw;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class CreatedDoneDto {
    private Integer createdTaskCount;
    private Integer completedTaskCount;
}
