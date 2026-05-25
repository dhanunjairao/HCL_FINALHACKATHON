package com.foodorder.controller;

import com.foodorder.dto.ApiResponse;
import com.foodorder.dto.UserDtos.*;
import com.foodorder.enums.Role;
import com.foodorder.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.foodorder.security.UserPrincipal;

import java.util.List;

@RestController
@RequestMapping("/users")
@CrossOrigin
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Users", description = "Admin endpoints for listing and managing users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Create a user account of any role (including ADMIN). Admin only â€” this is the only way to create admins.")
    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody CreateUserRequest req) {
        return ResponseEntity.ok(userService.create(req));
    }

    @Operation(summary = "List all users. Optionally filter by role. Admin only.")
    @GetMapping
    public ResponseEntity<List<UserResponse>> list(@RequestParam(required = false) Role role) {
        return ResponseEntity.ok(role == null ? userService.listAll() : userService.listByRole(role));
    }

    @Operation(summary = "Get a single user by id. Admin only.")
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getById(id));
    }

    @Operation(summary = "Update a user's role. Pass restaurantId when assigning the MANAGER role. Admin only.")
    @PutMapping("/{id}/role")
    public ResponseEntity<UserResponse> updateRole(@PathVariable Long id, @Valid @RequestBody UpdateRoleRequest req) {
        return ResponseEntity.ok(userService.updateRole(id, req));
    }

    @Operation(summary = "Enable or disable a user account. Admin only.")
    @PutMapping("/{id}/status")
    public ResponseEntity<UserResponse> updateStatus(@PathVariable Long id, @Valid @RequestBody UpdateStatusRequest req) {
        return ResponseEntity.ok(userService.updateStatus(id, req));
    }

    @Operation(summary = "Delete a user. Admins cannot delete their own account. Admin only.")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> delete(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        userService.delete(id, principal.getUsername());
        return ResponseEntity.ok(ApiResponse.success("User deleted"));
    }
}
