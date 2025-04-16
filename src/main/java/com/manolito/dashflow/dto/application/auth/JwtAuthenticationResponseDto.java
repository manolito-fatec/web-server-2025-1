package com.manolito.dashflow.dto.application.auth;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JwtAuthenticationResponseDto {
    private String token;
}