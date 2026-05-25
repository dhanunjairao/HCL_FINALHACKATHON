package com.foodorder.service;

import com.foodorder.dto.UserDtos.*;
import com.foodorder.entity.Restaurant;
import com.foodorder.entity.User;
import com.foodorder.enums.Role;
import com.foodorder.exception.BadRequestException;
import com.foodorder.exception.ResourceNotFoundException;
import com.foodorder.repository.RestaurantRepository;
import com.foodorder.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, RestaurantRepository restaurantRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.restaurantRepository = restaurantRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /** Admin-only account creation. Bypasses self-service flow; can create any role including ADMIN. */
    public UserResponse create(CreateUserRequest req) {
        if (userRepository.existsByUsername(req.getUsername())) {
            throw new BadRequestException("Username already taken");
        }
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new BadRequestException("Email already registered");
        }
        Restaurant restaurant = null;
        if (req.getRole() == Role.MANAGER && req.getRestaurantId() != null) {
            restaurant = restaurantRepository.findById(req.getRestaurantId())
                    .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found: " + req.getRestaurantId()));
        }
        User user = User.builder()
                .username(req.getUsername())
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .fullName(req.getFullName())
                .phone(req.getPhone())
                .address(req.getAddress())
                .role(req.getRole())
                .managedRestaurant(restaurant)
                .enabled(true)
                .build();
        return toResponse(userRepository.save(user));
    }

    public List<UserResponse> listAll() {
        return userRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<UserResponse> listByRole(Role role) {
        return userRepository.findByRole(role).stream().map(this::toResponse).collect(Collectors.toList());
    }

    public UserResponse getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    public UserResponse updateRole(Long id, UpdateRoleRequest req) {
        User user = findOrThrow(id);
        user.setRole(req.getRole());
        if (req.getRole() == Role.MANAGER) {
            if (req.getRestaurantId() == null) {
                throw new BadRequestException("Restaurant ID is required for MANAGER role");
            }
            Restaurant restaurant = restaurantRepository.findById(req.getRestaurantId())
                    .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found: " + req.getRestaurantId()));
            user.setManagedRestaurant(restaurant);
        } else {
            user.setManagedRestaurant(null);
        }
        return toResponse(userRepository.save(user));
    }

    public UserResponse updateStatus(Long id, UpdateStatusRequest req) {
        User user = findOrThrow(id);
        user.setEnabled(req.getEnabled());
        return toResponse(userRepository.save(user));
    }

    public void delete(Long id, String requestingUsername) {
        User user = findOrThrow(id);
        if (user.getUsername().equals(requestingUsername)) {
            throw new BadRequestException("You cannot delete your own account");
        }
        userRepository.delete(user);
    }

    private User findOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }

    private UserResponse toResponse(User u) {
        Restaurant r = u.getManagedRestaurant();
        return new UserResponse(
                u.getId(),
                u.getUsername(),
                u.getEmail(),
                u.getFullName(),
                u.getPhone(),
                u.getAddress(),
                u.getRole(),
                r != null ? r.getId() : null,
                r != null ? r.getName() : null,
                u.isEnabled(),
                u.getCreatedAt()
        );
    }
}
