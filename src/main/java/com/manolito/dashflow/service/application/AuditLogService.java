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

    /**
     * Retrieves filtered audit logs with pagination.
     * <p>
     * Supports filtering by:
     * <ul>
     *   <li>User ID</li>
     *   <li>Response status</li>
     *   <li>Date range (start and end dates)</li>
     * </ul>
     * Applies defaults for pagination (page=0, size=20) with maximum page size of 100.
     * </p>
     *
     * @param filterRequest the filter criteria containing:
     *                      <ul>
     *                        <li>userId - the user ID to filter by (optional)</li>
     *                        <li>responseStatus - the HTTP status to filter by (optional)</li>
     *                        <li>startDate - the beginning of date range (optional)</li>
     *                        <li>endDate - the end of date range (optional)</li>
     *                        <li>page - the page number (defaults to 0)</li>
     *                        <li>size - the page size (defaults to 20, max 100)</li>
     *                      </ul>
     * @return a {@link Page} of {@link AuditLogDto} containing the filtered results
     * @see AuditLogFilterRequestDto
     * @see AuditLogDto
     *
     * @example
     * <pre>{@code
     * // Get first page of logs for user 123 with 404 errors
     * AuditLogFilterRequestDto filters = AuditLogFilterRequestDto.builder()
     *     .userId(123)
     *     .responseStatus(404)
     *     .build();
     * Page<AuditLogDto> logs = auditLogService.getFilteredAuditLogs(filters);
     * }</pre>
     */
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
