package com.manolito.dashflow.repository.application;

import com.manolito.dashflow.entity.application.Account;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {
}