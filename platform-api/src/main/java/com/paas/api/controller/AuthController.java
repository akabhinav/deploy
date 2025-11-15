package com.paas.api.controller;

import com.paas.common.dto.AuthResponse;
import com.paas.common.dto.LoginRequest;
import com.paas.common.dto.RegisterRequest;
import com.paas.core.entity.User;
import com.paas.core.security.JwtTokenProvider;
import com.paas.core.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "User authentication and registration")
public class AuthController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/register")
    @Operation(summary = "Register new user")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("POST /api/v1/auth/register - Registering user: {}", request.getUsername());

        User user = userService.createUser(
                request.getUsername(),
                request.getEmail(),
                request.getPassword(),
                request.getFullName()
        );

        String token = jwtTokenProvider.generateToken(
                user.getId(),
                user.getEmail(),
                "default", // No organization yet
                "USER"
        );

        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        AuthResponse response = AuthResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .expiresIn(86400L) // 24 hours
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    @Operation(summary = "Login user")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("POST /api/v1/auth/login - User logging in: {}", request.getUsername());

        if (!userService.validateCredentials(request.getUsername(), request.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = userService.getUserByUsername(request.getUsername());
        userService.updateLastLogin(user.getId());

        String token = jwtTokenProvider.generateToken(
                user.getId(),
                user.getEmail(),
                "default",
                "USER"
        );

        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        AuthResponse response = AuthResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .expiresIn(86400L)
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh JWT token")
    public ResponseEntity<AuthResponse> refreshToken(@RequestParam String refreshToken) {
        log.info("POST /api/v1/auth/refresh - Refreshing token");

        if (!jwtTokenProvider.validateToken(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        User user = userService.getUserById(userId);

        String newToken = jwtTokenProvider.generateToken(
                user.getId(),
                user.getEmail(),
                "default",
                "USER"
        );

        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        AuthResponse response = AuthResponse.builder()
                .token(newToken)
                .refreshToken(newRefreshToken)
                .expiresIn(86400L)
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .build();

        return ResponseEntity.ok(response);
    }
}
