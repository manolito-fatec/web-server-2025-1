package com.manolito.dashflow.dto.dw;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class StatusCountDto {
    private String statusName;
    private Integer count;
}
