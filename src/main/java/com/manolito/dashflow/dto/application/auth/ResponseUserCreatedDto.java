package com.manolito.dashflow.dto.application.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseUserCreatedDto {
     private Integer userId;
     private String username;
     private String email;
     private String role;
}
