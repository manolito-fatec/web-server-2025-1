package com.manolito.dashflow.dto.application.auth;

import lombok.*;

import java.util.Set;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SignupRequestDto {
    private String username;
    private String email;
    private String password;
    private Set<String> roles;
    private String toolUserId;
    private Integer toolId;
}
