package com.foodorder.controller;

import com.foodorder.dto.ApiResponse;
import com.foodorder.dto.CartDtos.*;
import com.foodorder.security.UserPrincipal;
import com.foodorder.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
@CrossOrigin
@Tag(name = "Cart", description = "Persistent shopping cart for the logged in user")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('USER')")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @Operation(summary = "Get the current user's cart. Creates an empty cart if one does not exist.")
    @GetMapping
    public ResponseEntity<CartResponse> getCart(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(cartService.getCart(principal.getUsername()));
    }

    @Operation(summary = "Add a menu item to the cart. Adding from a different restaurant clears existing items.")
    @PostMapping("/items")
    public ResponseEntity<CartResponse> addItem(@Valid @RequestBody AddToCartRequest req,
                                                @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(cartService.addItem(principal.getUsername(), req));
    }

    @Operation(summary = "Update the quantity of a cart item. Sending zero or less removes the item.")
    @PutMapping("/items/{cartItemId}")
    public ResponseEntity<CartResponse> updateQuantity(@PathVariable Long cartItemId,
                                                       @Valid @RequestBody UpdateQuantityRequest req,
                                                       @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(cartService.updateItemQuantity(principal.getUsername(), cartItemId, req.getQuantity()));
    }

    @Operation(summary = "Remove a single item from the cart")
    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<CartResponse> removeItem(@PathVariable Long cartItemId,
                                                   @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(cartService.removeItem(principal.getUsername(), cartItemId));
    }

    @Operation(summary = "Empty the cart completely")
    @DeleteMapping
    public ResponseEntity<ApiResponse> clearCart(@AuthenticationPrincipal UserPrincipal principal) {
        cartService.clearCart(principal.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Cart cleared"));
    }
}
