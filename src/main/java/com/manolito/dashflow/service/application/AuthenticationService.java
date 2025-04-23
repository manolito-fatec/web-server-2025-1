package com.manolito.dashflow.service.application;

import com.manolito.dashflow.dto.application.auth.JwtAuthenticationResponseDto;
import com.manolito.dashflow.dto.application.auth.LoginRequestDto;
import com.manolito.dashflow.dto.application.auth.SignupRequestDto;
import com.manolito.dashflow.entity.application.ApplicationUser;
import com.manolito.dashflow.entity.application.Role;
import com.manolito.dashflow.repository.application.ApplicationUserRepository;
import com.manolito.dashflow.repository.application.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final ApplicationUserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ApplicationUserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public JwtAuthenticationResponseDto signup(SignupRequestDto request) {
        Set<Role> roles = roleRepository.findByRoleNameIn(request.getRoles());
        if (roles.size() != request.getRoles().size()) {
            throw new IllegalArgumentException("One or more roles are invalid");
        }

        var user = ApplicationUser
                .builder()
                .email(request.getEmail())
                .username(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(roles)
                .build();

        userRepository.save(user);
        var jwt = jwtService.generateToken(user);
        return JwtAuthenticationResponseDto.builder().token(jwt).build();
    }

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

    public Boolean getUserByUsername(String username) {
        return username != null
                && !username.isBlank()
                && userRepository.findByUsername(username).isPresent();
    }
}
