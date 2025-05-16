package com.manolito.dashflow.config;

import com.manolito.dashflow.dto.application.ApplicationUserDto;
import com.manolito.dashflow.entity.application.Account;
import com.manolito.dashflow.entity.application.ApplicationTool;
import com.manolito.dashflow.entity.application.ApplicationUser;
import com.manolito.dashflow.entity.application.Role;
import com.manolito.dashflow.repository.application.AccountRepository;
import com.manolito.dashflow.repository.application.ApplicationToolRepository;
import com.manolito.dashflow.repository.application.ApplicationUserRepository;
import com.manolito.dashflow.repository.application.RoleRepository;
import com.manolito.dashflow.service.application.ApplicationUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Component
@RequiredArgsConstructor
@Slf4j
public class SeedDataConfig implements CommandLineRunner {

    private final ApplicationUserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ApplicationUserService userService;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationToolRepository toolRepository;
    private final AccountRepository accountRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() == 0) {
            ApplicationTool taiga = createTool("taiga");
            ApplicationTool trello = createTool("trello");
            ApplicationTool jira = createTool("jira");

            ApplicationUser admin = createUser("admin", "admin@admin.com", "admin", "ROLE_ADMIN");
            ApplicationUser andre = createUser("Andre", "andre.andre@andre.com", "andre", "ROLE_OPERATOR");
            ApplicationUser bia = createUser("Bia", "bia.bia@bia.com", "bia", "ROLE_OPERATOR");
            ApplicationUser caue = createUser("Caue", "caue.caue@caue.com", "caue", "ROLE_OPERATOR");

            createAccount(andre, taiga, "755290", roleRepository.getReferenceById(1),"1637322");
            createAccount(bia, taiga, "758256", roleRepository.getReferenceById(1),"1637322");
            createAccount(caue, taiga, "754575", roleRepository.getReferenceById(1),"1637322");

            log.info("Database seeding completed successfully");
        }
    }

    private ApplicationTool createTool(String name) {
        ApplicationTool tool = ApplicationTool.builder()
                .toolName(name)
                .build();
        return toolRepository.save(tool);
    }

    private ApplicationUser createUser(String username, String email, String password, String role) {
        ApplicationUserDto userDto = ApplicationUserDto.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(password))
                .roles(Collections.singleton(role))
                .build();
        return userService.createUserEntity(userDto);
    }

    private void createAccount(ApplicationUser user, ApplicationTool tool, String accountId, Role role, String project) {
        Account account = Account.builder()
                .applicationUser(user)
                .tool(tool)
                .accountIdTool(accountId)
                .roleId(role)
                .projectIdTool(project)
                .build();
        accountRepository.save(account);
    }
}
