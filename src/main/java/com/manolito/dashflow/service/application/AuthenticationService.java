package com.manolito.dashflow.service.application;

import com.manolito.dashflow.dto.application.auth.JwtAuthenticationResponseDto;
import com.manolito.dashflow.dto.application.auth.LoginRequestDto;
import com.manolito.dashflow.dto.application.auth.ResponseUserCreatedDto;
import com.manolito.dashflow.dto.application.auth.SignupRequestDto;
import com.manolito.dashflow.entity.application.*;
import com.manolito.dashflow.repository.application.AccountRepository;
import com.manolito.dashflow.repository.application.ApplicationToolRepository;
import com.manolito.dashflow.repository.application.ApplicationUserRepository;
import com.manolito.dashflow.repository.application.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import javax.transaction.Transactional;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final ApplicationUserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final ApplicationToolRepository applicationToolRepository;
    private final AccountRepository accountRepository;

    /**
     * Handles the user signup process by validating the input request,
     * creating a new {@link ApplicationUser}, saving it to the repository,
     * creating the associated {@link Account}, and returning a simplified DTO
     * with user details.
     *
     * <p>This method is transactional to ensure that user creation and account
     * creation happen atomically.</p>
     *
     * @param request the {@link SignupRequestDto} containing user registration data
     * @return a {@link ResponseUserCreatedDto} containing selected details of the newly registered user
     * @throws IllegalArgumentException if the request is invalid, roles are missing/invalid, or tool is not found
     */
    @Transactional
    public ResponseUserCreatedDto signup(SignupRequestDto request) {
        validateRequest(request);
        Set<Role> roles = roleRepository.findByRoleNameIn(request.getRoles());
        Optional<ApplicationTool> tool = applicationToolRepository.findById(request.getToolId());
        validateFindRoleAndTool(roles, tool);

        var user = ApplicationUser
                .builder()
                .email(request.getEmail())
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(roles)
                .build();

        ApplicationUser registeredUser =  userRepository.save(user);
        createAccount(registeredUser, request.getToolUserId(), tool.get());

        return new ResponseUserCreatedDto(
                  registeredUser.getId(),
                  registeredUser.getUsername(),
                  registeredUser.getEmail(),
                  registeredUser.getRoleNames().stream().findFirst().get());
    }

    /**
     * Authenticates a user using the provided login credentials and returns a JWT token upon success.
     *
     * <p>The method attempts to authenticate the user using the Spring Security {@link AuthenticationManager}.
     * If authentication is successful, it retrieves the corresponding {@link ApplicationUser} from the repository
     * and generates a JWT token using the {@link JwtService}.</p>
     *
     * @param request the {@link LoginRequestDto} containing the user's email and password
     * @return a {@link JwtAuthenticationResponseDto} containing the generated JWT token
     * @throws IllegalArgumentException if authentication fails or the user is not found
     */
    public JwtAuthenticationResponseDto login(LoginRequestDto request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()));

            var user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));


            var jwt = jwtService.generateToken(user);
            return JwtAuthenticationResponseDto.builder().token(jwt).build();
        } catch (AuthenticationException e) {
            throw new IllegalArgumentException("Invalid credentials", e);
        }
    }

    /**
     * Checks whether a user exists with the given username.
     *
     * <p>The method returns {@code true} if the provided username is not null,
     * not blank, and a user with that username exists in the system.</p>
     *
     * @param username the username to search for
     * @return {@code true} if the username is valid and exists; {@code false} otherwise
     */
    public Boolean getUserByUsername(String username) {
        return username != null
                && !username.isBlank()
                && userRepository.findByUsername(username).isPresent();
    }

    /**
     * Creates and persists an {@link Account} entity linking the given user to the specified tool.
     *
     * <p>This method constructs a new {@code Account} using the provided user, tool, and external tool-specific user ID.
     * It then saves the account to the repository.</p>
     *
     * @param applicationUser the user to be associated with the tool
     * @param userIdTool the identifier of the user within the tool (external or tool-specific ID)
     * @param tool the {@link ApplicationTool} to associate with the user
     */
   private void createAccount(ApplicationUser applicationUser, String userIdTool, ApplicationTool tool)
   {
        Account account = Account.builder()
                .id(new AccountId(applicationUser.getId(), tool.getId()))
                .applicationUser(applicationUser)
                .tool(tool)
                .accountId(userIdTool)
                .build();
        accountRepository.save(account);
    }

    /**
     * Validates the contents of a {@link SignupRequestDto} object.
     *
     * <p>This method ensures that:
     * <ul>
     *     <li>The request object is not null</li>
     *     <li>The roles list is not null or empty</li>
     *     <li>Required fields (email, username, password, toolUserId) are not null or blank</li>
     *     <li>The toolId is not null</li>
     * </ul>
     *
     * @param request the {@code SignupRequestDto} to validate
     * @throws IllegalArgumentException if any field is invalid or missing
     */
    protected void validateRequest(SignupRequestDto request)
    {
        if (request == null)
        {
            throw new IllegalArgumentException("Request cannot be null");
        }

        if (request.getRoles() == null || request.getRoles().isEmpty())
        {
            throw new IllegalArgumentException("One or more roles are invalid");
        }

        validateField(request.getEmail(), "Email");
        validateField(request.getUsername(), "Username");
        validateField(request.getPassword(), "Password");
        validateField(request.getToolUserId(), "UserProjectId");
        if (request.getToolId() == null)
        {
            throw new IllegalArgumentException("ToolId cannot be null");
        }
    }

    /**
     * Validates that the given string field is neither null nor blank.
     *
     * @param field the string value to validate
     * @param fieldName the name of the field being validated, used for error messaging
     * @throws IllegalArgumentException if the field is null or blank
     */
    private void validateField(String field, String fieldName)
    {
        if (field == null || field.isBlank())
        {
            throw new IllegalArgumentException(fieldName + " cannot be null or blank");
        }
    }

    /**
     * Validates the presence of a valid tool and a non-empty set of roles.
     *
     * @param roles the set of roles to validate
     * @param tool the optional tool to validate
     * @throws IllegalArgumentException if the tool is not present or the roles set is null or empty
     */
    protected void validateFindRoleAndTool(Set<Role> roles, Optional<ApplicationTool> tool)
    {
        if(tool.isEmpty()){
            throw new IllegalArgumentException("Not a valid tool");
        }

        if(roles.isEmpty()){
            throw new IllegalArgumentException("Not a valid role");
        }
    }
}
