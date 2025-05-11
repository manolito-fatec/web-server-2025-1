package com.manolito.dashflow.service.application;

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

import java.util.Optional;
import java.util.Set;
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
        SignupRequestDto signupRequestDto = getValidRequest();
        ApplicationTool applicationTool = new ApplicationTool(1,"taiga");
        var user = ApplicationUser
                .builder()
                .email(signupRequestDto.getEmail())
                .username(signupRequestDto.getUsername())
                .password(signupRequestDto.getPassword())
                .roles(Set.of(new Role()))
                .build();
        var account = Account.builder()
                .id(new AccountId(user.getId(), applicationTool.getId()))
                .applicationUser(user)
                .tool(applicationTool)
                .accountId("123")
                .build();

        when(passwordEncoder.encode(user.getPassword()))
                .thenReturn(signupRequestDto.getPassword());

        when(roleRepository.findByRoleNameIn(signupRequestDto.getRoles()))
                .thenReturn(Set.of(new Role()));

        when(applicationToolRepository.findById(signupRequestDto.getToolId()))
                .thenReturn(Optional.of(applicationTool));

        when(userRepository.save(any(ApplicationUser.class)))
                .thenReturn(user);

        when(accountRepository.save(any(Account.class)))
                .thenReturn(account);

        assertEquals(authenticationService.signup(signupRequestDto), user);
    }

    @Test
    @DisplayName("Should return new user when signup request is valid")
    void signup_shouldThrowException_whenValidateFindRoleAndToolIsInvalid() {
        SignupRequestDto signupRequestDto = getValidRequest();
        ApplicationTool applicationTool = new ApplicationTool(1,"taiga");

        // Tool ID search returned empty

        when(roleRepository.findByRoleNameIn(signupRequestDto.getRoles()))
                .thenReturn(Set.of(new Role()));

        when(applicationToolRepository.findById(signupRequestDto.getToolId()))
                .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                authenticationService.signup(signupRequestDto));

        // Role search returned empty

        when(roleRepository.findByRoleNameIn(signupRequestDto.getRoles()))
                .thenReturn(Set.of());

        when(applicationToolRepository.findById(signupRequestDto.getToolId()))
                .thenReturn(Optional.of(applicationTool));

        assertThrows(IllegalArgumentException.class, () ->
                authenticationService.signup(signupRequestDto));
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
    void validateRequest_shouldThrowException_whenRequestAnyFileIsInvalid(String username, String email, String password, Set<String> roles, String toolUserId, Integer toolId)
    {
        SignupRequestDto signupRequestDto = new SignupRequestDto(username, email, password, roles, toolUserId,toolId);
        assertThrows(IllegalArgumentException.class, () ->
                authenticationService.validateRequest(signupRequestDto));
    }

    private static Stream<Arguments> provideInputAndExpectedValues()
    {
        return Stream.of(
                Arguments.of("", "nome1@lp2.com", "123", Set.of("ROLE_OPERATOR"), "789", 1),
                Arguments.of(null, "nome1@lp2.com", "123", Set.of("ROLE_OPERATOR"), "789", 1),
                Arguments.of("patolino", "", "123", Set.of("ROLE_OPERATOR"), "789", 1),
                Arguments.of("patolino", null, "123", Set.of("ROLE_OPERATOR"), "789", 1),
                Arguments.of("patolino", "nome1@lp2.com", "", Set.of("ROLE_OPERATOR"), "789", 1),
                Arguments.of("patolino", "nome1@lp2.com", null, Set.of("ROLE_OPERATOR"), "789", 1),
                Arguments.of("patolino", "nome1@lp2.com", "123", Set.of(), "789", 1),
                Arguments.of("patolino", "nome1@lp2.com", "123", null, "789", 1),
                Arguments.of("patolino", "nome1@lp2.com", "123", Set.of("ROLE_OPERATOR"), "", 1),
                Arguments.of("patolino", "nome1@lp2.com", "123", Set.of("ROLE_OPERATOR"), null, 1),
                Arguments.of("patolino", "nome1@lp2.com", "123", Set.of("ROLE_OPERATOR"), "789", null)
        );
    }

    private SignupRequestDto getValidRequest()
    {
        SignupRequestDto request = new SignupRequestDto();
        request.setEmail("test@example.com");
        request.setUsername("testuser");
        request.setPassword("password123");
        request.setToolUserId("external-user-id");
        request.setToolId(1);
        request.setRoles(Set.of("ROLE_OPERATOR"));
        return request;
    }
}