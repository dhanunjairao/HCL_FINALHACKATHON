package com.example.HCL_FInal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

public class MenuItemDtos {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MenuItemRequest {
        @NotBlank
        private String name;
        private String description;
        @NotNull
        @Positive
        private BigDecimal price;
        private String category;
        private String imageUrl;
        private boolean available = true;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MenuItemResponse {
        private Long id;
        private String name;
        private String description;
        private BigDecimal price;
        private String category;
        private String imageUrl;
        private boolean available;
        private Long restaurantId;
        private String restaurantName;
    }
}