package com.manolito.dashflow.service.application;

import com.manolito.dashflow.dto.application.AuditLogDto;
import com.manolito.dashflow.dto.application.AuditLogFilterRequestDto;
import com.manolito.dashflow.entity.application.AuditLog;
import com.manolito.dashflow.repository.application.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuditLogService {
    private final AuditLogRepository auditLogRepository;

    public Page<AuditLogDto> getFilteredAuditLogs(AuditLogFilterRequestDto filterRequest) {

        int page = filterRequest.getPage() != null ? filterRequest.getPage() : 0;
        int size = filterRequest.getSize() != null ? Math.min(filterRequest.getSize(), 100) : 20;

        Pageable pageable = PageRequest.of(page, size);

        Page<AuditLog> logs = auditLogRepository.findFilteredLogs(
                filterRequest.getUserId(),
                filterRequest.getResponseStatus(),
                filterRequest.getStartDate(),
                filterRequest.getEndDate(),
                pageable
        );

        return logs.map(AuditLogDto::fromEntity);
    }
}
