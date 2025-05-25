package com.manolito.dashflow.dto.dw;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TrelloAuthDto {
    private String token;
    private String apiKey;
}
