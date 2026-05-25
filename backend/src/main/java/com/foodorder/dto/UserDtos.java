package com.foodorder.dto;

import com.foodorder.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class UserDtos {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserResponse {
        private Long id;
        private String username;
        private String email;
        private String fullName;
        private String phone;
        private String address;
        private Role role;
        private Long managedRestaurantId;
        private String managedRestaurantName;
        private boolean enabled;
        private LocalDateTime createdAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRoleRequest {
        @NotNull
        private Role role;
        private Long restaurantId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateStatusRequest {
        @NotNull
        private Boolean enabled;
    }

    /**
     * Admin-only account creation. Bypasses self-service registration entirely
     * and is the only path that can create ADMIN accounts. For MANAGER role,
     * pass an existing restaurantId (it does not go through verification).
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateUserRequest {
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
        @NotNull
        private Role role;
        private Long restaurantId; // optional; for MANAGER role
    }
}
