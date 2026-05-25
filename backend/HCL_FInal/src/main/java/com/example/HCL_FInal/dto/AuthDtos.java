package com.example.HCL_FInal.dto;

import com.example.HCL_FInal.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

public class AuthDtos {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginRequest {
        @NotBlank
        private String username;
        @NotBlank
        private String password;
    }

    /**
     * Self-service signup. Only USER and MANAGER are accepted.
     * ADMIN accounts can only be created by existing admins via POST /users.
     *
     * If role = MANAGER, the proprietor/restaurant fields are required and the
     * restaurant is created in PENDING status. The manager account stays disabled
     * until an admin approves the restaurant.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegisterRequest {
        @NotBlank
        @Size(min = 3, max = 50)
        private String username;
        @NotBlank
        @Email
        private String email;
        @NotBlank
        @Size(min = 6)
        private String password;
        private String fullName;
        private String phone;
        private String address;
        private Role role; // optional; defaults to USER. ADMIN is rejected.

        // ----- MANAGER-only fields. Required when role = MANAGER. -----
        private String proprietorName;
        private String restaurantName;
        private String foodLicense;
        private String restaurantAddress;
        private Double latitude;
        private Double longitude;
        private String cuisineType;
        private String restaurantPhone;
        private String restaurantImageUrl;
        private String restaurantDescription;
        private LocalTime openTime;
        private LocalTime closeTime;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuthResponse {
        private String token;
        private String username;
        private String email;
        private Role role;
        private Long userId;
        private Long restaurantId;
    }
}