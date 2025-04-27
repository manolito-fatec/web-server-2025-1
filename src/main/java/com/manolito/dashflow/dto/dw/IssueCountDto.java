package com.manolito.dashflow.dto.dw;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class IssueCountDto {
    private final String type;
    private final Integer count;
}
