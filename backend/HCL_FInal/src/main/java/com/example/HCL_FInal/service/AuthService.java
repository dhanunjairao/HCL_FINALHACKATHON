package com.example.HCL_FInal.service;

import com.example.HCL_FInal.dto.ApiResponse;
import com.example.HCL_FInal.dto.AuthDtos.*;
import com.example.HCL_FInal.entity.Restaurant;
import com.example.HCL_FInal.entity.User;
import com.example.HCL_FInal.enums.Role;
import com.example.HCL_FInal.enums.VerificationStatus;
import com.example.HCL_FInal.exception.BadRequestException;
import com.example.HCL_FInal.exception.ResourceNotFoundException;
import com.example.HCL_FInal.repository.RestaurantRepository;
import com.example.HCL_FInal.repository.UserRepository;
import com.example.HCL_FInal.security.JwtUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    public AuthService(UserRepository userRepository, RestaurantRepository restaurantRepository,
                       PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager,
                       JwtUtils jwtUtils) {
        this.userRepository = userRepository;
        this.restaurantRepository = restaurantRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
    }

    @Transactional
    public ApiResponse register(RegisterRequest req) {
        if (userRepository.existsByUsername(req.getUsername())) {
            throw new BadRequestException("Username already taken");
        }
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new BadRequestException("Email already registered");
        }

        Role role = req.getRole() != null ? req.getRole() : Role.USER;
        if (role == Role.ADMIN) {
            throw new BadRequestException("Admin accounts can only be created by an existing admin");
        }

        if (role == Role.MANAGER) {
            return registerManager(req);
        }
        // USER and DELIVERY both self-register with no approval required.
        return registerAutoEnabled(req, role);
    }

    private ApiResponse registerAutoEnabled(RegisterRequest req, Role role) {
        User user = User.builder()
                .username(req.getUsername())
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .fullName(req.getFullName())
                .phone(req.getPhone())
                .address(req.getAddress())
                .role(role)
                .enabled(true)
                .build();
        userRepository.save(user);
        logger.info("{} registered and logged in: {}", role, user.getUsername());
        AuthResponse auth = login(new LoginRequest(req.getUsername(), req.getPassword()));
        return ApiResponse.success("Registration successful", auth);
    }

    private ApiResponse registerManager(RegisterRequest req) {
        requireNotBlank(req.getProprietorName(), "proprietorName");
        requireNotBlank(req.getRestaurantName(), "restaurantName");
        requireNotBlank(req.getFoodLicense(), "foodLicense");
        requireNotBlank(req.getRestaurantAddress(), "restaurantAddress");

        Restaurant restaurant = Restaurant.builder()
                .name(req.getRestaurantName())
                .description(req.getRestaurantDescription())
                .cuisineType(req.getCuisineType())
                .address(req.getRestaurantAddress())
                .phone(req.getRestaurantPhone())
                .imageUrl(req.getRestaurantImageUrl())
                .proprietorName(req.getProprietorName())
                .foodLicense(req.getFoodLicense())
                .latitude(req.getLatitude())
                .longitude(req.getLongitude())
                .openTime(req.getOpenTime())
                .closeTime(req.getCloseTime())
                .verificationStatus(VerificationStatus.PENDING)
                .active(true)
                .build();
        restaurant = restaurantRepository.save(restaurant);

        User user = User.builder()
                .username(req.getUsername())
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .fullName(req.getFullName())
                .phone(req.getPhone())
                .address(req.getAddress())
                .role(Role.MANAGER)
                .managedRestaurant(restaurant)
                .enabled(false)
                .build();
        userRepository.save(user);

        logger.info("Manager registration submitted: username={}, restaurant={} (PENDING)",
                user.getUsername(), restaurant.getName());
        return ApiResponse.success(
                "Registration submitted. Your restaurant is pending admin approval — you will be able to log in once approved.",
                null);
    }

    public AuthResponse login(LoginRequest req) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = jwtUtils.generateJwtToken(authentication);
        User user = userRepository.findByUsername(req.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Long restaurantId = user.getManagedRestaurant() != null ? user.getManagedRestaurant().getId() : null;
        logger.info("User logged in: {}", user.getUsername());
        return new AuthResponse(token, user.getUsername(), user.getEmail(), user.getRole(), user.getId(), restaurantId);
    }

    private static void requireNotBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new BadRequestException(fieldName + " is required for manager registration");
        }
    }
}
