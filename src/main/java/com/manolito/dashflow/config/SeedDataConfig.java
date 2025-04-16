package com.manolito.dashflow.config;

import com.manolito.dashflow.dto.application.ApplicationUserDto;
import com.manolito.dashflow.repository.application.ApplicationUserRepository;
import com.manolito.dashflow.service.application.ApplicationUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
@RequiredArgsConstructor
@Slf4j
public class SeedDataConfig implements CommandLineRunner {
    private final ApplicationUserRepository userRepository;
    private final ApplicationUserService userService;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 3) {
            ApplicationUserDto admin = ApplicationUserDto
                    .builder()
                    .email("admin@admin.com")
                    .username("admin")
                    .password("admin")
                    .roles(Collections.singleton("ROLE_ADMIN"))
                    .build();

            userService.createUser(admin);
            log.debug("Created admin user - {}", admin);
        }
    }
}
