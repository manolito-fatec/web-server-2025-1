package com.manolito.dashflow.service.application;

import com.manolito.dashflow.dto.application.auth.ResponseUserCreatedDto;
import com.manolito.dashflow.dto.application.auth.SignupRequestDto;

import com.manolito.dashflow.entity.application.*;
import com.manolito.dashflow.repository.application.AccountRepository;
import com.manolito.dashflow.repository.application.ApplicationToolRepository;
import com.manolito.dashflow.repository.application.ApplicationUserRepository;
import com.manolito.dashflow.repository.application.RoleRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import static org.mockito.Mockito.*;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @InjectMocks
    private AuthenticationService authenticationService;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private ApplicationToolRepository applicationToolRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private ApplicationUserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("Should return new user when signup request is valid")
    void signup_shouldReturnNewUser_whenSignupIsValid() {
        SignupRequestDto request = new SignupRequestDto();
        request.setEmail("john@example.com");
        request.setUsername("john");
        request.setPassword("123456");
        request.setRoles(Set.of("ROLE_ADMIN"));
        request.setToolId(1);
        request.setToolUserId("john123");
        request.setToolProjectIdList(List.of("proj-001", "proj-002"));

        Role role = new Role(1, "ROLE_ADMIN", Set.of());
        ApplicationTool tool = new ApplicationTool(1, "Jira");

        when(roleRepository.findByRoleNameIn(request.getRoles()))
                .thenReturn(Set.of(role));

        when(applicationToolRepository.findById(request.getToolId()))
                .thenReturn(Optional.of(tool));

        when(passwordEncoder.encode(request.getPassword()))
                .thenReturn("encoded123");

        ApplicationUser savedUser = ApplicationUser.builder()
                .id(42)
                .email(request.getEmail())
                .username(request.getUsername())
                .password("encoded123")
                .roles(Set.of(role))
                .build();

        when(userRepository.save(any(ApplicationUser.class)))
                .thenReturn(savedUser);

        ResponseUserCreatedDto result = authenticationService.signup(request);

        assertEquals(Optional.of(42).get(), result.getUserId());
        assertEquals("john", result.getUsername());
        assertEquals("john@example.com", result.getEmail());

        verify(userRepository).save(any(ApplicationUser.class));
        verify(accountRepository, times(2)).save(any(Account.class));
    }

    @Test
    @DisplayName("Should throw exception when request is null")
    void validateRequest_shouldThrowException_whenRequestIsNull()
    {
        assertThrows(IllegalArgumentException.class, () ->
                authenticationService.validateRequest(null));
    }

    @ParameterizedTest
    @MethodSource("provideInputAndExpectedValues")
    @DisplayName("Should throw exception when any field in SignupRequestDto is invalid")
    void validateRequest_shouldThrowException_whenRequestAnyFileIsInvalid(
            String username, String email, String password,
            Set<String> roles, String toolUserId,
            List<String> toolProjectIdList, Integer toolId) {
        SignupRequestDto signupRequestDto = SignupRequestDto.builder()
                .username(username)
                .email(email)
                .password(password)
                .roles(roles)
                .toolUserId(toolUserId)
                .toolProjectIdList(toolProjectIdList)
                .toolId(toolId)
                .build();

        assertThrows(IllegalArgumentException.class, () ->
                authenticationService.validateRequest(signupRequestDto));
    }

    private static Stream<Arguments> provideInputAndExpectedValues() {
        return Stream.of(
                Arguments.of("", "nome1@lp2.com", "123", Set.of("ROLE_OPERATOR"), "789", List.of("32432"), 1),
                Arguments.of(null, "nome1@lp2.com", "123", Set.of("ROLE_OPERATOR"), "789", List.of("32432"), 1),
                Arguments.of("patolino", "", "123", Set.of("ROLE_OPERATOR"), "789", List.of("32432"), 1),
                Arguments.of("patolino", null, "123", Set.of("ROLE_OPERATOR"), "789", List.of("32432"), 1),
                Arguments.of("patolino", "nome1@lp2.com", "", Set.of("ROLE_OPERATOR"), "789", List.of("32432"), 1),
                Arguments.of("patolino", "nome1@lp2.com", null, Set.of("ROLE_OPERATOR"), "789", List.of("32432"), 1),
                Arguments.of("patolino", "nome1@lp2.com", "123", Set.of(), "789", List.of("32432"), 1),
                Arguments.of("patolino", "nome1@lp2.com", "123", null, "789", List.of("32432"), 1),
                Arguments.of("patolino", "nome1@lp2.com", "123", Set.of("ROLE_OPERATOR"), "", List.of("32432"), 1),
                Arguments.of("patolino", "nome1@lp2.com", "123", Set.of("ROLE_OPERATOR"), null, List.of("32432"), 1),
                Arguments.of("patolino", "nome1@lp2.com", "123", Set.of("ROLE_OPERATOR"), "789", List.of("32432"), null),
                Arguments.of("patolino", "nome1@lp2.com", "123", Set.of("ROLE_OPERATOR"), "789", List.of(""), 1),
                Arguments.of("patolino", "nome1@lp2.com", "123", Set.of("ROLE_OPERATOR"), "789", null, 1),
                Arguments.of("patolino", "nome1@lp2.com", "123", Set.of("ROLE_OPERATOR"), "789", Arrays.asList("32432", null), 1),
                Arguments.of("patolino", "nome1@lp2.com", "123", Set.of("ROLE_OPERATOR"), "789", Collections.emptyList(), 1)
        );
    }

    private SignupRequestDto getValidRequest() {
        SignupRequestDto request = new SignupRequestDto();
        request.setEmail("test@example.com");
        request.setUsername("testuser");
        request.setPassword("password123");
        request.setToolUserId("external-user-id");
        request.setToolId(1);
        request.setRoles(Set.of("ROLE_OPERATOR"));
        request.setToolProjectIdList(List.of("project-001", "project-002"));
        return request;
    }
}