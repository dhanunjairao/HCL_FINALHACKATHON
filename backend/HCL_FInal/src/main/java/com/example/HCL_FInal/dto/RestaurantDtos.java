package com.example.HCL_FInal.dto;

import com.example.HCL_FInal.enums.VerificationStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

public class RestaurantDtos {
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RestaurantRequest {
        @NotBlank
        private String name;
        private String description;
        private String cuisineType;
        private String address;
        private String phone;
        private String imageUrl;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VerificationRequest {
        @NotNull
        private VerificationStatus status;
        private String rejectionReason;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimingsRequest {
        @NotNull
        private LocalTime openTime;
        @NotNull
        private LocalTime closeTime;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RestaurantResponse {
        private Long id;
        private String name;
        private String description;
        private String cuisineType;
        private String address;
        private String phone;
        private String imageUrl;
        private String proprietorName;
        private String foodLicense;
        private Double latitude;
        private Double longitude;
        private LocalTime openTime;
        private LocalTime closeTime;
        private VerificationStatus verificationStatus;
        private String rejectionReason;
        private boolean active;
        private boolean openNow;
        private int menuItemCount;
    }
}