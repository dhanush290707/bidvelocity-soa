package com.bidvelocity.auth.controller;

import com.bidvelocity.auth.dto.AuthResponse;
import com.bidvelocity.auth.dto.LoginRequest;
import com.bidvelocity.auth.dto.RegisterRequest;
import com.bidvelocity.auth.entity.User;
import com.bidvelocity.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/validate")
    public ResponseEntity<AuthResponse> validate(@RequestHeader("Authorization") String authHeader) {
        AuthResponse response = authService.validateToken(authHeader);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(@RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
                                                              @RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (userIdHeader != null && !userIdHeader.isBlank()) {
            User user = authService.getUserById(Long.parseLong(userIdHeader));
            return ResponseEntity.ok(Map.of(
                    "id", user.getId(),
                    "username", user.getUsername(),
                    "email", user.getEmail(),
                    "role", user.getRole().name()
            ));
        } else if (authHeader != null && !authHeader.isBlank()) {
            AuthResponse validated = authService.validateToken(authHeader);
            return ResponseEntity.ok(Map.of(
                    "id", validated.getUserId(),
                    "username", validated.getUsername(),
                    "email", validated.getEmail(),
                    "role", validated.getRole().name()
            ));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}
