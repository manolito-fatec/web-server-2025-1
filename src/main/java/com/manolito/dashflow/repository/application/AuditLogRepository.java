package com.manolito.dashflow.repository.application;

import com.manolito.dashflow.entity.application.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface AuditLogRepository extends JpaRepository<AuditLog, Integer> {

    @Query("""
            SELECT a FROM AuditLog a WHERE
            (:userId IS NULL OR a.userId = :userId) AND
            (:responseStatus IS NULL OR a.responseStatus = :responseStatus) AND
            (CAST(:startDate AS timestamp) IS NULL OR a.timestamp >= :startDate) AND
            (CAST(:endDate AS timestamp) IS NULL OR a.timestamp <= :endDate)
            ORDER BY a.timestamp DESC
            """)
    Page<AuditLog> findFilteredLogs(
            @Param("userId") Integer userId,
            @Param("responseStatus") Integer responseStatus,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);
}
