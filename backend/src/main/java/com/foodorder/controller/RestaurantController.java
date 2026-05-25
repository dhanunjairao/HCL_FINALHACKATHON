package com.foodorder.controller;

import com.foodorder.dto.ApiResponse;
import com.foodorder.dto.MenuItemDtos.MenuItemResponse;
import com.foodorder.dto.RestaurantDtos.*;
import com.foodorder.exception.BadRequestException;
import com.foodorder.security.UserPrincipal;
import com.foodorder.service.MenuItemService;
import com.foodorder.service.RestaurantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/restaurants")
@CrossOrigin
@Tag(name = "Restaurants", description = "Restaurant browsing, registration, verification and management")
public class RestaurantController {

    private final RestaurantService restaurantService;
    private final MenuItemService menuItemService;

    public RestaurantController(RestaurantService restaurantService, MenuItemService menuItemService) {
        this.restaurantService = restaurantService;
        this.menuItemService = menuItemService;
    }

    @Operation(summary = "List approved, active restaurants. Public.")
    @GetMapping
    public ResponseEntity<List<RestaurantResponse>> listPublic() {
        return ResponseEntity.ok(restaurantService.listPublic());
    }

    @Operation(summary = "Search approved restaurants by name, cuisine, or address. Public.")
    @GetMapping("/search")
    public ResponseEntity<List<RestaurantResponse>> search(@RequestParam(required = false) String q,
                                                           @RequestParam(required = false) Boolean openNow) {
        return ResponseEntity.ok(restaurantService.search(q, openNow));
    }

    @Operation(summary = "Admin listing including pending/rejected/inactive restaurants")
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<RestaurantResponse>> listAll() {
        return ResponseEntity.ok(restaurantService.listAll());
    }

    @Operation(summary = "List restaurants awaiting verification. Admin only.")
    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<RestaurantResponse>> listPending() {
        return ResponseEntity.ok(restaurantService.listPending());
    }

    @Operation(summary = "Fetch a single restaurant by id")
    @GetMapping("/{id}")
    public ResponseEntity<RestaurantResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(restaurantService.getById(id));
    }

    @Operation(summary = "Fetch the public menu for a restaurant. Only available items are returned.")
    @GetMapping("/{id}/menu")
    public ResponseEntity<List<MenuItemResponse>> getMenu(@PathVariable Long id) {
        return ResponseEntity.ok(menuItemService.listAvailableByRestaurant(id));
    }

    @Operation(summary = "Approve or reject a restaurant. Admin only.")
    @PutMapping("/{id}/verify")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RestaurantResponse> verify(@PathVariable Long id,
                                                     @Valid @RequestBody VerificationRequest req) {
        return ResponseEntity.ok(restaurantService.verify(id, req));
    }

    @Operation(summary = "Set restaurant open/close timings. Manager (own restaurant) or admin.")
    @PutMapping("/{id}/timings")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public ResponseEntity<RestaurantResponse> updateTimings(@PathVariable Long id,
                                                            @Valid @RequestBody TimingsRequest req,
                                                            @AuthenticationPrincipal UserPrincipal principal) {
        ensureManagerOwns(principal, id);
        return ResponseEntity.ok(restaurantService.updateTimings(id, req));
    }

    @Operation(summary = "Update an existing restaurant. Admin only.")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RestaurantResponse> update(@PathVariable Long id, @Valid @RequestBody RestaurantRequest req) {
        return ResponseEntity.ok(restaurantService.update(id, req));
    }

    @Operation(summary = "Delete a restaurant and its menu. Admin only.")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> delete(@PathVariable Long id) {
        restaurantService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Restaurant deleted"));
    }

    private void ensureManagerOwns(UserPrincipal principal, Long restaurantId) {
        if ("ROLE_ADMIN".equals(principal.getRole())) return;
        if (principal.getRestaurantId() == null || !principal.getRestaurantId().equals(restaurantId)) {
            throw new BadRequestException("Managers can only manage their own restaurant");
        }
    }
}
