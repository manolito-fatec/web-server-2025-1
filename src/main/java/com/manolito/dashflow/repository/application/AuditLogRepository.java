package com.manolito.dashflow.repository.application;

import com.manolito.dashflow.entity.application.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
}
