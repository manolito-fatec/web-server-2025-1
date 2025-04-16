package com.manolito.dashflow.repository.application;

import com.manolito.dashflow.entity.application.ApplicationTool;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationToolRepository extends JpaRepository<ApplicationTool, Integer> {
    ApplicationTool findByToolName(String toolName);
}