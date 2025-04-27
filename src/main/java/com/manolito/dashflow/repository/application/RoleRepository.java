package com.manolito.dashflow.repository.application;

import com.manolito.dashflow.entity.application.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.Set;

public interface RoleRepository extends JpaRepository<Role, Integer> {
    Set<Role> findByRoleNameIn(Set<String> roleNames);
    Optional<Role> findByRoleName(String roleName);
}
