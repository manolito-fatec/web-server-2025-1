package com.manolito.dashflow.dto.application;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApplicationUserDto {
    private Integer id;
    private String username;
    private String password;
    private String email;
    private LocalDateTime createdAt;
    private Set<String> roles;
    private Set<String> permissions;
}