package com.example.HCL_FInal.dto;

import com.example.HCL_FInal.enums.OrderStatus;
import com.example.HCL_FInal.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrderDtos {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemRequest {
        @NotNull
        private Long menuItemId;
        @NotNull
        @Positive
        private Integer quantity;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderRequest {
        // Both restaurantId and items are optional. If omitted the order is
        // built from the user's persistent cart. If supplied, the request body
        // wins and the cart is ignored.
        private Long restaurantId;
        private List<OrderItemRequest> items;
        private String deliveryAddress;
        private String phone;
        private String notes;
        private PaymentMethod paymentMethod; // optional, defaults to CASH_ON_DELIVERY
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemResponse {
        private Long id;
        private Long menuItemId;
        private String menuItemName;
        private Integer quantity;
        private BigDecimal price;
        private BigDecimal subtotal;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderResponse {
        private Long id;
        private Long userId;
        private String username;
        private String customerName;
        private Long restaurantId;
        private String restaurantName;
        private List<OrderItemResponse> items;
        private BigDecimal totalAmount;
        private OrderStatus status;
        private String deliveryAddress;
        private String phone;
        private String notes;
        private PaymentMethod paymentMethod;
        private Long deliveryPersonId;
        private String deliveryPersonName;
        private LocalDateTime orderDate;
        private LocalDateTime updatedAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatusUpdateRequest {
        @NotNull
        private OrderStatus status;
    }
}