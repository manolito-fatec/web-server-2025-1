package com.manolito.dashflow.dto.dw;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TaskTagDto {
    private String tagName;
    private Integer count;
}
