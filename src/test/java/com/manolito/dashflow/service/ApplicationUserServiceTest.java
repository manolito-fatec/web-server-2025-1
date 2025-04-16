package com.manolito.dashflow.service;

import com.manolito.dashflow.dto.application.ApplicationUserDto;
import com.manolito.dashflow.entity.application.ApplicationUser;
import com.manolito.dashflow.entity.application.Role;
import com.manolito.dashflow.repository.application.ApplicationUserRepository;
import com.manolito.dashflow.repository.application.RoleRepository;
import com.manolito.dashflow.service.application.ApplicationUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplicationUserServiceTest {

    @Mock
    private ApplicationUserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private ApplicationUserService userService;

    private ApplicationUser testUser;
    private ApplicationUserDto testUserDto;
    private Set<Role> testRoles;

    @BeforeEach
    void setUp() {
        testRoles = new HashSet<>();
        Role adminRole = new Role();
        adminRole.setId(1);
        adminRole.setRoleName("ROLE_ADMIN");

        Role userRole = new Role();
        userRole.setId(2);
        userRole.setRoleName("ROLE_USER");

        testRoles.add(adminRole);
        testRoles.add(userRole);

        testUser = new ApplicationUser();
        testUser.setId(1);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setRoles(testRoles);

        testUserDto = ApplicationUserDto.builder()
                .id(1)
                .username("testuser")
                .email("test@example.com")
                .password("password")
                .roles(Set.of("ROLE_ADMIN", "ROLE_USER"))
                .build();
    }

    @Test
    @DisplayName("Should return user DTO when user exists with given ID")
    void getUserById_whenUserExists_shouldReturnUserDto() {
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));

        ApplicationUserDto result = userService.getUserById(1);

        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        assertEquals(testUser.getUsername(), result.getUsername());
        assertEquals(testUser.getEmail(), result.getEmail());
        assertEquals(testUser.getPassword(), result.getPassword());
        assertEquals(2, result.getRoles().size());
        verify(userRepository, times(1)).findById(1);
    }

    @Test
    @DisplayName("Should throw NoSuchElementException when no user exists with given ID")
    void getUserById_whenUserNotFound_shouldThrowNoSuchElementException() {
        when(userRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> userService.getUserById(1));
        verify(userRepository, times(1)).findById(1);
    }

    @Test
    @DisplayName("Should return user DTO when user exists with given email")
    void getUserByEmail_whenUserExists_shouldReturnUserDto() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        ApplicationUserDto result = userService.getUserByEmail("test@example.com");

        assertNotNull(result);
        assertEquals(testUser.getEmail(), result.getEmail());
        assertEquals(2, result.getRoles().size());
        verify(userRepository, times(1)).findByEmail("test@example.com");
    }

    @Test
    @DisplayName("Should throw NoSuchElementException when no user exists with given email")
    void getUserByEmail_whenUserNotFound_shouldThrowNoSuchElementException() {
        when(userRepository.findByEmail("gasparzinho@example.com")).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> userService.getUserByEmail("gasparzinho@example.com"));
        verify(userRepository, times(1)).findByEmail("gasparzinho@example.com");
    }

    @Test
    @DisplayName("Should return list of user DTOs when users exist")
    void getAllUsers_whenUsersExist_shouldReturnListOfUserDtos() {
        List<ApplicationUser> users = Collections.singletonList(testUser);
        when(userRepository.findAll()).thenReturn(users);

        List<ApplicationUserDto> result = userService.getAllUsers();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testUser.getId(), result.get(0).getId());
        assertEquals(2, result.get(0).getRoles().size());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should throw NoSuchElementException when no users exist")
    void getAllUsers_whenNoUsersExist_shouldThrowNoSuchElementException() {
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        assertThrows(NoSuchElementException.class, () -> userService.getAllUsers());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return created user DTO when valid input is provided")
    void createUser_whenValidInput_shouldReturnCreatedUserDto() {
        when(roleRepository.findByRoleNameIn(anySet())).thenReturn(testRoles);
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(userRepository.save(any(ApplicationUser.class))).thenReturn(testUser);

        ApplicationUserDto result = userService.createUser(testUserDto);

        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        assertEquals(2, result.getRoles().size());
        verify(roleRepository, times(1)).findByRoleNameIn(testUserDto.getRoles());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when provided roles don't exist")
    void createUser_whenRolesMismatch_shouldThrowIllegalArgumentException() {
        Set<Role> returnedRoles = new HashSet<>();
        returnedRoles.add(testRoles.iterator().next()); // Add a single role

        when(roleRepository.findByRoleNameIn(anySet())).thenReturn(returnedRoles);

        // Set more than the ONE role added earlier
        testUserDto.setRoles(Set.of("ROLE_ADMIN", "ROLE_USER", "NON_EXISTENT_ROLE"));

        assertThrows(IllegalArgumentException.class, () -> userService.createUser(testUserDto));
        verify(roleRepository, times(1)).findByRoleNameIn(testUserDto.getRoles());
    }

    @Test
    @DisplayName("Should return updated user DTO when user exists")
    void updateUser_whenUserExists_shouldReturnUpdatedUserDto() {
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(roleRepository.findByRoleNameIn(anySet())).thenReturn(testRoles);
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(userRepository.save(any(ApplicationUser.class))).thenReturn(testUser);

        ApplicationUserDto result = userService.updateUser(testUserDto);

        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        assertEquals(2, result.getRoles().size());
        verify(userRepository, times(1)).findById(1);
    }

    @Test
    @DisplayName("Should throw NoSuchElementException when updating non-existent user")
    void updateUser_whenUserNotFound_shouldThrowNoSuchElementException() {
        when(userRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> userService.updateUser(testUserDto));
        verify(userRepository, times(1)).findById(1);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when updating with non-existent roles")
    void updateUser_whenRolesMismatch_shouldThrowIllegalArgumentException() {
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        when(roleRepository.findByRoleNameIn(anySet())).thenReturn(Collections.singleton(testRoles.iterator().next()));

        assertThrows(IllegalArgumentException.class, () -> userService.updateUser(testUserDto));
        verify(userRepository, times(1)).findById(1);
    }

    @Test
    @DisplayName("Should return deleted user DTO when user exists")
    void deleteUser_whenUserExists_shouldReturnDeletedUserDto() {
        when(userRepository.findById(1)).thenReturn(Optional.of(testUser));
        doNothing().when(userRepository).deleteById(1);

        ApplicationUserDto result = userService.deleteUser(1);

        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        assertEquals(2, result.getRoles().size());
        verify(userRepository, times(1)).deleteById(1);
    }

    @Test
    @DisplayName("Should throw NoSuchElementException when deleting non-existent user")
    void deleteUser_whenUserNotFound_shouldThrowNoSuchElementException() {
        when(userRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> userService.deleteUser(1));
        verify(userRepository, times(1)).findById(1);
    }

    @Test
    @DisplayName("Should return UserDetails when user exists with given email")
    void loadUserByUsername_whenUserExists_shouldReturnUserDetails() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        UserDetails result = userService.loadUserByUsername("test@example.com");

        assertNotNull(result);
        assertEquals(testUser.getUsername(), result.getUsername());
    }

    @Test
    @DisplayName("Should throw UsernameNotFoundException when no user exists with given username")
    void loadUserByUsername_whenUserNotFound_shouldThrowUsernameNotFoundException() {
        String username = "gasparzinho";

        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> userService.loadUserByUsername(username)
        );

        assertEquals("User not found with username: " + username, exception.getMessage());
    }

    @Test
    @DisplayName("createUserEntity - With valid DTO - Should return saved user with correct attributes")
    void createUserEntity_withValidDto_shouldReturnSavedUser() {
        when(roleRepository.findByRoleNameIn(anySet())).thenReturn(testRoles);
        when(userRepository.save(any(ApplicationUser.class))).thenAnswer(invocation -> {
            ApplicationUser user = invocation.getArgument(0);
            user.setId(1);
            return user;
        });

        ApplicationUser result = userService.createUserEntity(testUserDto);

        assertNotNull(result);
        assertEquals(testUserDto.getUsername(), result.getUsername());
        assertEquals(testUserDto.getEmail(), result.getEmail());
        assertEquals(testUserDto.getPassword(), result.getPassword());
        assertEquals(testRoles, result.getRoles());
        assertNotNull(result.getCreatedAt());
        verify(roleRepository, times(1)).findByRoleNameIn(testUserDto.getRoles());
        verify(userRepository, times(1)).save(any(ApplicationUser.class));
    }

    @Test
    @DisplayName("createUserEntity - With null DTO - Should throw IllegalArgumentException")
    void createUserEntity_withNullDto_shouldThrowException() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.createUserEntity(null)
        );
        assertEquals("User DTO cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("createUserEntity - With null username - Should throw IllegalArgumentException")
    void createUserEntity_withNullUsername_shouldThrowException() {
        testUserDto.setUsername(null);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.createUserEntity(testUserDto)
        );
        assertEquals("Username cannot be null or empty", exception.getMessage());
    }

    @Test
    @DisplayName("createUserEntity - With empty username - Should throw IllegalArgumentException")
    void createUserEntity_withEmptyUsername_shouldThrowException() {
        testUserDto.setUsername("");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.createUserEntity(testUserDto)
        );
        assertEquals("Username cannot be null or empty", exception.getMessage());
    }

    @Test
    @DisplayName("createUserEntity - With non-existent roles - Should throw IllegalArgumentException")
    void createUserEntity_withNonExistentRoles_shouldThrowException() {
        when(roleRepository.findByRoleNameIn(anySet())).thenReturn(Set.of());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.createUserEntity(testUserDto)
        );
        assertTrue(exception.getMessage().contains("One or more roles don't exist"));
        verify(roleRepository, times(1)).findByRoleNameIn(testUserDto.getRoles());
    }

    @Test
    @DisplayName("createUserEntity - With null email - Should throw IllegalArgumentException")
    void createUserEntity_withNullEmail_shouldThrowException() {
        testUserDto.setEmail(null);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.createUserEntity(testUserDto)
        );
        assertEquals("Email cannot be null or empty", exception.getMessage());
    }

    @Test
    @DisplayName("createUserEntity - With null password - Should throw IllegalArgumentException")
    void createUserEntity_withNullPassword_shouldThrowException() {
        testUserDto.setPassword(null);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.createUserEntity(testUserDto)
        );
        assertEquals("Password cannot be null or empty", exception.getMessage());
    }
}