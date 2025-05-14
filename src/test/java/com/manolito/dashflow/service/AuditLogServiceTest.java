package com.manolito.dashflow.service;

import com.manolito.dashflow.dto.application.AuditLogDto;
import com.manolito.dashflow.dto.application.AuditLogFilterRequestDto;
import com.manolito.dashflow.entity.application.AuditLog;
import com.manolito.dashflow.repository.application.AuditLogRepository;
import com.manolito.dashflow.service.application.AuditLogService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditLogService auditLogService;

    private final LocalDateTime TEST_START_DATE = LocalDateTime.parse("2024-01-01T00:00:00");
    private final LocalDateTime TEST_END_DATE = LocalDateTime.parse("2024-01-31T23:59:59");
    private final Integer TEST_USER_ID = 123;
    private final Integer TEST_RESPONSE_STATUS = 200;

    @Test
    @DisplayName("getFilteredAuditLogs - should return paginated logs when filters are provided")
    void getFilteredAuditLogs_whenFiltersProvided_shouldReturnPaginatedLogs() {
        AuditLogFilterRequestDto filterRequest = AuditLogFilterRequestDto.builder()
                .userId(TEST_USER_ID)
                .responseStatus(TEST_RESPONSE_STATUS)
                .startDate(TEST_START_DATE)
                .endDate(TEST_END_DATE)
                .page(1)
                .size(20)
                .build();

        Pageable pageable = PageRequest.of(1, 20);
        List<AuditLog> mockLogs = List.of(
                createTestAuditLog(1, TEST_USER_ID, TEST_RESPONSE_STATUS, TEST_START_DATE.plusDays(1)),
                createTestAuditLog(2, TEST_USER_ID, TEST_RESPONSE_STATUS, TEST_START_DATE.plusDays(2))
        );
        Page<AuditLog> mockPage = new PageImpl<>(mockLogs, pageable, 2);

        when(auditLogRepository.findFilteredLogs(
                TEST_USER_ID,
                TEST_RESPONSE_STATUS,
                TEST_START_DATE,
                TEST_END_DATE,
                pageable))
                .thenReturn(mockPage);

        Page<AuditLogDto> result = auditLogService.getFilteredAuditLogs(filterRequest);

        assertEquals(2, result.getContent().size());
        assertEquals(TEST_USER_ID, result.getContent().get(0).getUserId());
        assertEquals(TEST_RESPONSE_STATUS, result.getContent().get(0).getResponseStatus());
        verify(auditLogRepository).findFilteredLogs(
                TEST_USER_ID,
                TEST_RESPONSE_STATUS,
                TEST_START_DATE,
                TEST_END_DATE,
                pageable
        );
    }

    @Test
    @DisplayName("getFilteredAuditLogs - should return empty page when no logs match filters")
    void getFilteredAuditLogs_whenNoMatches_shouldReturnEmptyPage() {
        AuditLogFilterRequestDto filterRequest = AuditLogFilterRequestDto.builder()
                .userId(999)
                .build();

        Pageable pageable = PageRequest.of(0, 20);
        Page<AuditLog> mockPage = Page.empty(pageable);

        when(auditLogRepository.findFilteredLogs(
                999,
                null,
                null,
                null,
                pageable))
                .thenReturn(mockPage);

        Page<AuditLogDto> result = auditLogService.getFilteredAuditLogs(filterRequest);

        assertTrue(result.isEmpty());
        verify(auditLogRepository).findFilteredLogs(
                999,
                null,
                null,
                null,
                pageable);
    }

    @Test
    @DisplayName("getFilteredAuditLogs - should use default pagination when not specified")
    void getFilteredAuditLogs_whenPaginationNotSpecified_shouldUseDefaults() {
        AuditLogFilterRequestDto filterRequest = AuditLogFilterRequestDto.builder().build();
        Pageable defaultPageable = PageRequest.of(0, 20);
        Page<AuditLog> mockPage = new PageImpl<>(List.of(
                createTestAuditLog(1, null, null, TEST_START_DATE)
        ));

        when(auditLogRepository.findFilteredLogs(null, null, null, null, defaultPageable))
                .thenReturn(mockPage);

        Page<AuditLogDto> result = auditLogService.getFilteredAuditLogs(filterRequest);

        assertEquals(1, result.getContent().size());
        verify(auditLogRepository).findFilteredLogs(null, null, null, null, defaultPageable);
    }

    @Test
    @DisplayName("getFilteredAuditLogs - should cap page size at maximum allowed value")
    void getFilteredAuditLogs_whenPageSizeExceedsMax_shouldCapAtMaximum() {
        // Arrange
        AuditLogFilterRequestDto filterRequest = AuditLogFilterRequestDto.builder()
                .size(150)
                .build();

        Pageable cappedPageable = PageRequest.of(0, 100);
        Page<AuditLog> mockPage = new PageImpl<>(List.of(
                createTestAuditLog(1, null, null, TEST_START_DATE)
        ));

        when(auditLogRepository.findFilteredLogs(
                null,
                null,
                null, null,
                cappedPageable))
                .thenReturn(mockPage);

        Page<AuditLogDto> result = auditLogService.getFilteredAuditLogs(filterRequest);

        assertEquals(1, result.getContent().size());
        verify(auditLogRepository).findFilteredLogs(
                null,
                null,
                null,
                null,
                cappedPageable);
    }

    private AuditLog createTestAuditLog(Integer logId, Integer userId, Integer responseStatus, LocalDateTime timestamp) {
        return AuditLog.builder()
                .logId(logId)
                .userId(userId)
                .requestMethod("GET")
                .requestUri("/api/test")
                .responseStatus(responseStatus)
                .timestamp(timestamp)
                .build();
    }
}