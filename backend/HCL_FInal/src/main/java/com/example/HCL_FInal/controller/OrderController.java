package com.example.HCL_FInal.controller;

import com.example.HCL_FInal.dto.OrderDtos.*;
import com.example.HCL_FInal.security.UserPrincipal;
import com.example.HCL_FInal.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
@CrossOrigin
@Tag(name = "Orders", description = "Order placement, tracking and management")
@SecurityRequirement(name = "bearerAuth")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @Operation(summary = "Place a new order from the user's cart contents or item list")
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<OrderResponse> placeOrder(@Valid @RequestBody OrderRequest req,
                                                    @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(orderService.placeOrder(principal.getUsername(), req));
    }

    @Operation(summary = "Get the logged in user's order history")
    @GetMapping("/my-orders")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<OrderResponse>> myOrders(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(orderService.getOrdersForUser(principal.getUsername()));
    }

    @Operation(summary = "Cancel a pending order. Only the owner can cancel and only if status is PENDING")
    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<OrderResponse> cancel(@PathVariable Long id,
                                                @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(orderService.cancelOrder(id, principal.getUsername()));
    }

    @Operation(summary = "Get all orders for the manager's restaurant")
    @GetMapping("/restaurant")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<OrderResponse>> restaurantOrders(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(orderService.getOrdersForRestaurant(principal.getRestaurantId()));
    }

    @Operation(summary = "Update order status. Managers can only update their own restaurant's orders")
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public ResponseEntity<OrderResponse> updateStatus(@PathVariable Long id,
                                                      @Valid @RequestBody StatusUpdateRequest req,
                                                      @AuthenticationPrincipal UserPrincipal principal) {
        Long managerRestaurantId = "ROLE_MANAGER".equals(principal.getRole()) ? principal.getRestaurantId() : null;
        return ResponseEntity.ok(orderService.updateStatus(id, req.getStatus(), managerRestaurantId));
    }

    @Operation(summary = "Admin view of every order in the system")
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OrderResponse>> allOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @Operation(summary = "Delivery queue: READY orders not yet claimed by any delivery person")
    @GetMapping("/available-deliveries")
    @PreAuthorize("hasRole('DELIVERY')")
    public ResponseEntity<List<OrderResponse>> availableDeliveries() {
        return ResponseEntity.ok(orderService.listAvailableForDelivery());
    }

    @Operation(summary = "Orders assigned to the current delivery person (active + history)")
    @GetMapping("/my-deliveries")
    @PreAuthorize("hasRole('DELIVERY')")
    public ResponseEntity<List<OrderResponse>> myDeliveries(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(orderService.listForDeliveryPerson(principal.getUsername()));
    }

    @Operation(summary = "Delivery person claims a READY order; status moves to OUT_FOR_DELIVERY")
    @PostMapping("/{id}/claim-delivery")
    @PreAuthorize("hasRole('DELIVERY')")
    public ResponseEntity<OrderResponse> claimDelivery(@PathVariable Long id,
                                                       @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(orderService.claimDelivery(id, principal.getUsername()));
    }

    @Operation(summary = "Delivery person marks their assigned order as DELIVERED")
    @PostMapping("/{id}/mark-delivered")
    @PreAuthorize("hasRole('DELIVERY')")
    public ResponseEntity<OrderResponse> markDelivered(@PathVariable Long id,
                                                       @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(orderService.markDelivered(id, principal.getUsername()));
    }

    @Operation(summary = "Fetch a single order by id")
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<OrderResponse> getOne(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getById(id));
    }
}
