package com.example.HCL_FInal.controller;

import com.example.HCL_FInal.dto.ApiResponse;
import com.example.HCL_FInal.dto.AuthDtos.*;
import com.example.HCL_FInal.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@CrossOrigin
@Tag(name = "Authentication", description = "Register and login endpoints. Public access.")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "Self-service registration. USER: auto-logged in with token. "
            + "MANAGER: creates account + restaurant in PENDING state, returns no token, awaits admin approval. "
            + "ADMIN role is rejected — admins are created only by other admins via POST /users.")
    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@Valid @RequestBody RegisterRequest req) {
        return ResponseEntity.ok(authService.register(req));
    }

    @Operation(summary = "Login with username and password to receive a JWT")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(authService.login(req));
    }
}
