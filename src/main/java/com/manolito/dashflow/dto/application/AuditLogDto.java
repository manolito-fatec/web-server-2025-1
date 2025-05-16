package com.manolito.dashflow.dto.application;

import com.manolito.dashflow.entity.application.AuditLog;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuditLogDto {
    private Integer logId;
    private Integer userId;
    private String requestMethod;
    private String requestUri;
    private Integer responseStatus;
    private String errorMessage;
    private LocalDateTime timestamp;

    public static AuditLogDto fromEntity(AuditLog auditLog) {
        return AuditLogDto.builder()
                .logId(auditLog.getLogId())
                .userId(auditLog.getUserId())
                .requestMethod(auditLog.getRequestMethod())
                .requestUri(auditLog.getRequestUri())
                .responseStatus(auditLog.getResponseStatus())
                .errorMessage(auditLog.getErrorMessage())
                .timestamp(auditLog.getTimestamp())
                .build();
    }
}
