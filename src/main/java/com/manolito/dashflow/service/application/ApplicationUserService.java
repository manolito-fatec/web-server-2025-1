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

import java.util.*;

@RequiredArgsConstructor
@Service
public class ApplicationUserService implements UserDetailsService {
    private final ApplicationUserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public ApplicationUserDto getUserById(Integer id) {
        Optional<ApplicationUser> user = userRepository.findById(id);
        if (user.isEmpty()) {
            throw new NoSuchElementException();
        }
        return convertUserToDto(user.get());
    }

    public ApplicationUserDto getUserByEmail(String email) {
        Optional<ApplicationUser> user = userRepository.findByEmail(email);
        if (user.isEmpty()) {
            throw new NoSuchElementException(email);
        }
        return convertUserToDto(user.get());
    }

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

    public ApplicationUserDto deleteUser(Integer id) {
        Optional<ApplicationUser> user = userRepository.findById(id);
        if (user.isEmpty()) {
            throw new NoSuchElementException("User not found");
        }
        userRepository.deleteById(id);
        return convertUserToDto(user.get());
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<ApplicationUser> user = userRepository.findByUsername(username);
        if (user.isEmpty()) {
            throw new UsernameNotFoundException("User not found with username: " + username);        }
        return user.get();
    }

    private ApplicationUserDto convertUserToDto(ApplicationUser applicationUser) {
        return ApplicationUserDto.builder()
                .id(applicationUser.getId())
                .username(applicationUser.getUsername())
                .email(applicationUser.getEmail())
                .password(applicationUser.getPassword())
                .roles(applicationUser.getRoleNames())
                .build();
    }
}