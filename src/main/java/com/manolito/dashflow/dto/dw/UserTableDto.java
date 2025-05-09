package com.manolito.dashflow.dto.dw;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserTableDto {
    private String originalId;
    private String userName;
    private String userRole;
    private String toolName;
    private Integer toolId;
    private String projectId;
    private String projectName;
    private LocalDate createdAt;
}
