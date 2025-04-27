package com.manolito.dashflow.service.application;

import com.manolito.dashflow.dto.application.ApplicationUserDto;
import com.manolito.dashflow.entity.application.ApplicationUser;
import com.manolito.dashflow.entity.application.Role;
import com.manolito.dashflow.repository.application.RoleRepository;
import com.manolito.dashflow.repository.application.ApplicationUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Service for managing application users and their authentication.
 * <p>
 * This service provides CRUD operations for users and implements Spring Security's
 * {@link UserDetailsService} for authentication purposes.
 * </p>
 */
@RequiredArgsConstructor
@Service
public class ApplicationUserService implements UserDetailsService {
    private final ApplicationUserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Retrieves a user by their ID.
     * <p>
     * This method fetches a user from the repository and converts it to a DTO.
     * </p>
     *
     * @param id the ID of the user to retrieve (must not be null)
     * @return the user DTO containing user information
     * @throws NoSuchElementException if no user is found with the given ID
     * @see ApplicationUserDto
     *
     * @example
     * <pre>{@code
     * // Get user with ID 123
     * ApplicationUserDto user = userService.getUserById(123);
     * }</pre>
     */
    public ApplicationUserDto getUserById(Integer id) {
        Optional<ApplicationUser> user = userRepository.findById(id);
        if (user.isEmpty()) {
            throw new NoSuchElementException();
        }
        return convertUserToDto(user.get());
    }

    /**
     * Retrieves a user by their email address.
     * <p>
     * This method searches for a user by their unique email address.
     * </p>
     *
     * @param email the email address of the user to retrieve (must not be null or empty)
     * @return the user DTO containing user information
     * @throws NoSuchElementException if no user is found with the given email
     * @throws IllegalArgumentException if email is null or empty
     * @see ApplicationUserDto
     *
     * @example
     * <pre>{@code
     * // Get user by email
     * ApplicationUserDto user = userService.getUserByEmail("user@example.com");
     * }</pre>
     */
    public ApplicationUserDto getUserByEmail(String email) {
        Optional<ApplicationUser> user = userRepository.findByEmail(email);
        if (user.isEmpty()) {
            throw new NoSuchElementException(email);
        }
        return convertUserToDto(user.get());
    }

    /**
     * Retrieves all users in the system.
     * <p>
     * This method fetches all users from the repository and converts them to DTOs.
     * </p>
     *
     * @return a list of user DTOs
     * @throws NoSuchElementException if no users exist in the system
     * @see ApplicationUserDto
     *
     * @example
     * <pre>{@code
     * // Get all users
     * List<ApplicationUserDto> users = userService.getAllUsers();
     * }</pre>
     */
    public List<ApplicationUserDto> getAllUsers() {
        List<ApplicationUser> applicationUserList = userRepository.findAll();
        if (applicationUserList.isEmpty()) {
            throw new NoSuchElementException("No users found");
        }
        List<ApplicationUserDto> applicationUserDtoList = new ArrayList<>();
        for (ApplicationUser applicationUser : applicationUserList) {
            applicationUserDtoList.add(convertUserToDto(applicationUser));
        }
        return applicationUserDtoList;
    }

    /**
     * Updates an existing user.
     * <p>
     * This method updates user information including username, email, password, and roles.
     * The password will be encoded before storage.
     * </p>
     *
     * @param applicationUserDto the DTO containing updated user information (must not be null)
     * @return the updated user DTO
     * @throws NoSuchElementException if no user exists with the given ID
     * @throws IllegalArgumentException if the provided roles don't match existing roles
     * @see ApplicationUserDto
     *
     * @example
     * <pre>{@code
     * // Update user
     * ApplicationUserDto updatedUser = userService.updateUser(userDto);
     * }</pre>
     */
    public ApplicationUserDto updateUser(ApplicationUserDto applicationUserDto) {
        ApplicationUser applicationUser = userRepository.findById(applicationUserDto.getId())
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        Set<Role> roles = roleRepository.findByRoleNameIn(applicationUserDto.getRoles());
        if (roles.size() != applicationUserDto.getRoles().size()) {
            throw new IllegalArgumentException("Roles mismatch!");
        }

        applicationUser.setUsername(applicationUserDto.getUsername());
        applicationUser.setEmail(applicationUserDto.getEmail());
        applicationUser.setPassword(passwordEncoder.encode(applicationUserDto.getPassword()));
        applicationUser.setRoles(roles);

        return convertUserToDto(userRepository.save(applicationUser));
    }

    /**
     * Creates a new user.
     * <p>
     * This method creates a new user with the provided information, encoding the password
     * and validating the assigned roles.
     * </p>
     *
     * @param applicationUserDto the DTO containing new user information (must not be null)
     * @return the created user DTO
     * @throws IllegalArgumentException if the provided roles don't exist in the system
     * @see ApplicationUserDto
     *
     * @example
     * <pre>{@code
     * // Create new user
     * ApplicationUserDto newUser = userService.createUser(userDto);
     * }</pre>
     */
    public ApplicationUserDto createUser(ApplicationUserDto applicationUserDto) {
        Set<Role> roles = roleRepository.findByRoleNameIn(applicationUserDto.getRoles());

        if (roles.size() != applicationUserDto.getRoles().size()) {
            throw new IllegalArgumentException("One or more roles don't exist");
        }

        ApplicationUser applicationUser = ApplicationUser.builder()
                .username(applicationUserDto.getUsername())
                .password(passwordEncoder.encode(applicationUserDto.getPassword()))
                .email(applicationUserDto.getEmail())
                .roles(roles)
                .build();

        applicationUser = userRepository.save(applicationUser);
        return convertUserToDto(applicationUser);
    }

    /**
     * Deletes a user by their ID.
     * <p>
     * This method removes a user from the system after verifying their existence.
     * </p>
     *
     * @param id the ID of the user to delete (must not be null)
     * @return the DTO of the deleted user
     * @throws NoSuchElementException if no user exists with the given ID
     * @see ApplicationUserDto
     *
     * @example
     * <pre>{@code
     * // Delete user with ID 123
     * ApplicationUserDto deletedUser = userService.deleteUser(123);
     * }</pre>
     */
    public ApplicationUserDto deleteUser(Integer id) {
        Optional<ApplicationUser> user = userRepository.findById(id);
        if (user.isEmpty()) {
            throw new NoSuchElementException("User not found");
        }
        userRepository.deleteById(id);
        return convertUserToDto(user.get());
    }

    /**
     * Loads user details by email for authentication.
     * <p>
     * This method implements Spring Security's {@link UserDetailsService} interface
     * to support user authentication.
     * </p>
     *
     * @param email the email to search for (must not be null or empty)
     * @return the user details for authentication
     * @throws UsernameNotFoundException if no user is found with the given email
     * @see UserDetails
     *
     * @example
     * <pre>{@code
     * // Used internally by Spring Security during authentication
     * UserDetails userDetails = userService.loadUserByUsername("test@example.com");
     * }</pre>
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<ApplicationUser> user = userRepository.findByEmail(email);
        if (user.isEmpty()) {
            throw new UsernameNotFoundException("User not found with username: " + email);
        }
        return user.get();
    }

    /**
     * Converts an ApplicationUser entity to its DTO representation.
     * <p>
     * This private method handles the conversion between the entity and DTO,
     * including mapping roles to their names.
     * </p>
     *
     * @param applicationUser the user entity to convert (must not be null)
     * @return the converted user DTO
     * @see ApplicationUserDto
     */
    private ApplicationUserDto convertUserToDto(ApplicationUser applicationUser) {
        return ApplicationUserDto.builder()
                .id(applicationUser.getId())
                .username(applicationUser.getUsername())
                .email(applicationUser.getEmail())
                .password(applicationUser.getPassword())
                .roles(applicationUser.getRoleNames())
                .build();
    }

    /**
     * Creates and saves a new ApplicationUser entity from the provided DTO.
     * <p>
     * This method converts an ApplicationUserDto to an ApplicationUser entity, assigns roles,
     * sets the creation timestamp, and persists the entity to the database.
     * </p>
     *
     * @param applicationUserDto the user data transfer object containing user information
     * @return the persisted ApplicationUser entity
     * @throws IllegalArgumentException if:
     *         - The DTO is null
     *         - Required fields (username, email, password) are null or empty
     *         - The specified roles don't exist in the database
     * @see ApplicationUserDto
     * @see ApplicationUser
     */
    public ApplicationUser createUserEntity(ApplicationUserDto applicationUserDto) {
        // Validate input
        if (applicationUserDto == null) {
            throw new IllegalArgumentException("User DTO cannot be null");
        }
        if (applicationUserDto.getUsername() == null || applicationUserDto.getUsername().isBlank()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        if (applicationUserDto.getEmail() == null || applicationUserDto.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        if (applicationUserDto.getPassword() == null || applicationUserDto.getPassword().isBlank()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }

        Set<Role> roles = roleRepository.findByRoleNameIn(applicationUserDto.getRoles());
        if (roles.size() != applicationUserDto.getRoles().size()) {
            throw new IllegalArgumentException("One or more roles don't exist");
        }

        ApplicationUser user = ApplicationUser.builder()
                .username(applicationUserDto.getUsername())
                .password(applicationUserDto.getPassword())
                .email(applicationUserDto.getEmail())
                .roles(roles)
                .createdAt(LocalDateTime.now())
                .build();

        return userRepository.save(user);
    }
}