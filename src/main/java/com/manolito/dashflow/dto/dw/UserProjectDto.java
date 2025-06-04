package com.manolito.dashflow.dto.dw;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserProjectDto {
    private String userId;
    private String userName;
    private String projectId;
    private String projectName;
}
