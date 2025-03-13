package com.manolito.dashflow.dto.dw;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthDTO {
    private String username;
    private String password;
    private String type;
}
