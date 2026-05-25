package com.example.HCL_FInal.controller;

import com.example.HCL_FInal.dto.ApiResponse;
import com.example.HCL_FInal.dto.MenuItemDtos.*;
import com.example.HCL_FInal.exception.BadRequestException;
import com.example.HCL_FInal.security.UserPrincipal;
import com.example.HCL_FInal.service.MenuItemService;
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
@RequestMapping("/menu-items")
@CrossOrigin
@Tag(name = "Menu Items", description = "Menu item CRUD for managers and admin")
@SecurityRequirement(name = "bearerAuth")
public class MenuItemController {

    private final MenuItemService menuItemService;

    public MenuItemController(MenuItemService menuItemService) {
        this.menuItemService = menuItemService;
    }

    @Operation(summary = "List all menu items for a restaurant. Manager must own the restaurant.")
    @GetMapping("/restaurant/{restaurantId}")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public ResponseEntity<List<MenuItemResponse>> listAll(@PathVariable Long restaurantId,
                                                          @AuthenticationPrincipal UserPrincipal principal) {
        ensureManagerOwnsRestaurant(principal, restaurantId);
        return ResponseEntity.ok(menuItemService.listByRestaurant(restaurantId));
    }

    @Operation(summary = "Create a new menu item under a restaurant")
    @PostMapping("/restaurant/{restaurantId}")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public ResponseEntity<MenuItemResponse> create(@PathVariable Long restaurantId,
                                                   @Valid @RequestBody MenuItemRequest req,
                                                   @AuthenticationPrincipal UserPrincipal principal) {
        ensureManagerOwnsRestaurant(principal, restaurantId);
        return ResponseEntity.ok(menuItemService.create(restaurantId, req));
    }

    @Operation(summary = "Update an existing menu item")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public ResponseEntity<MenuItemResponse> update(@PathVariable Long id,
                                                   @Valid @RequestBody MenuItemRequest req,
                                                   @AuthenticationPrincipal UserPrincipal principal) {
        Long restaurantId = menuItemService.getEntity(id).getRestaurant().getId();
        ensureManagerOwnsRestaurant(principal, restaurantId);
        return ResponseEntity.ok(menuItemService.update(id, req));
    }

    @Operation(summary = "Delete a menu item")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse> delete(@PathVariable Long id,
                                              @AuthenticationPrincipal UserPrincipal principal) {
        Long restaurantId = menuItemService.getEntity(id).getRestaurant().getId();
        ensureManagerOwnsRestaurant(principal, restaurantId);
        menuItemService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Menu item deleted"));
    }

    private void ensureManagerOwnsRestaurant(UserPrincipal principal, Long restaurantId) {
        if ("ROLE_ADMIN".equals(principal.getRole())) {
            return;
        }
        if (principal.getRestaurantId() == null || !principal.getRestaurantId().equals(restaurantId)) {
            throw new BadRequestException("Managers can only manage their own restaurant");
        }
    }
}
