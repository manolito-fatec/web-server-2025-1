package com.manolito.dashflow.repository.application;

import com.manolito.dashflow.entity.application.ApplicationUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ApplicationUserRepository extends JpaRepository<ApplicationUser, Integer> {
    Optional<ApplicationUser> findByUsername(String username);
    Optional<ApplicationUser> findByEmail(String userEmail);
}
