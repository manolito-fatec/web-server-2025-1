package com.manolito.dashflow.dto.dw;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TaigaAuthDto {
    private String username;
    private String password;
    private String type;
}
