package com.foodorder.service;

import com.foodorder.dto.CartDtos.*;
import com.foodorder.entity.Cart;
import com.foodorder.entity.CartItem;
import com.foodorder.entity.MenuItem;
import com.foodorder.entity.User;
import com.foodorder.exception.BadRequestException;
import com.foodorder.exception.ResourceNotFoundException;
import com.foodorder.repository.CartRepository;
import com.foodorder.repository.MenuItemRepository;
import com.foodorder.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class CartService {

    private static final Logger logger = LoggerFactory.getLogger(CartService.class);

    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final MenuItemRepository menuItemRepository;

    public CartService(CartRepository cartRepository,
                       UserRepository userRepository, MenuItemRepository menuItemRepository) {
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
        this.menuItemRepository = menuItemRepository;
    }

    public CartResponse getCart(String username) {
        Cart cart = getOrCreateCart(username);
        return toResponse(cart);
    }

    public CartResponse addItem(String username, AddToCartRequest req) {
        Cart cart = getOrCreateCart(username);
        MenuItem menuItem = menuItemRepository.findById(req.getMenuItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found: " + req.getMenuItemId()));

        if (!menuItem.isAvailable()) {
            throw new BadRequestException("Item not available: " + menuItem.getName());
        }

        // If cart belongs to a different restaurant, clear it first
        if (cart.getRestaurant() != null
                && !cart.getRestaurant().getId().equals(menuItem.getRestaurant().getId())) {
            logger.info("Clearing cart for user {} because new item is from a different restaurant", username);
            cart.getItems().clear();
        }
        cart.setRestaurant(menuItem.getRestaurant());

        // If the menu item already exists in the cart, increment quantity
        Optional<CartItem> existing = cart.getItems().stream()
                .filter(ci -> ci.getMenuItem().getId().equals(menuItem.getId()))
                .findFirst();

        if (existing.isPresent()) {
            CartItem ci = existing.get();
            ci.setQuantity(ci.getQuantity() + req.getQuantity());
        } else {
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .menuItem(menuItem)
                    .quantity(req.getQuantity())
                    .build();
            cart.getItems().add(newItem);
        }

        cart.setUpdatedAt(LocalDateTime.now());
        Cart saved = cartRepository.save(cart);
        logger.info("Added {} x {} to cart for user {}", req.getQuantity(), menuItem.getName(), username);
        return toResponse(saved);
    }

    public CartResponse updateItemQuantity(String username, Long cartItemId, Integer quantity) {
        Cart cart = getOrCreateCart(username);
        CartItem item = cart.getItems().stream()
                .filter(ci -> ci.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        if (quantity <= 0) {
            cart.getItems().remove(item);
        } else {
            item.setQuantity(quantity);
        }

        // If cart became empty, also clear restaurant binding
        if (cart.getItems().isEmpty()) {
            cart.setRestaurant(null);
        }

        cart.setUpdatedAt(LocalDateTime.now());
        Cart saved = cartRepository.save(cart);
        return toResponse(saved);
    }

    public CartResponse removeItem(String username, Long cartItemId) {
        return updateItemQuantity(username, cartItemId, 0);
    }

    public CartResponse clearCart(String username) {
        Cart cart = getOrCreateCart(username);
        cart.getItems().clear();
        cart.setRestaurant(null);
        cart.setUpdatedAt(LocalDateTime.now());
        Cart saved = cartRepository.save(cart);
        logger.info("Cart cleared for user {}", username);
        return toResponse(saved);
    }

    // Used by OrderService after a successful order placement
    public void clearCartForUser(Long userId) {
        cartRepository.findByUserId(userId).ifPresent(cart -> {
            cart.getItems().clear();
            cart.setRestaurant(null);
            cart.setUpdatedAt(LocalDateTime.now());
            cartRepository.save(cart);
        });
    }

    public Cart getOrCreateCart(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return cartRepository.findByUserId(user.getId()).orElseGet(() -> {
            Cart cart = Cart.builder()
                    .user(user)
                    .createdAt(LocalDateTime.now())
                    .build();
            return cartRepository.save(cart);
        });
    }

    private CartResponse toResponse(Cart cart) {
        List<CartItemResponse> itemResponses = cart.getItems().stream()
                .map(ci -> new CartItemResponse(
                        ci.getId(),
                        ci.getMenuItem().getId(),
                        ci.getMenuItem().getName(),
                        ci.getMenuItem().getImageUrl(),
                        ci.getMenuItem().getPrice(),
                        ci.getQuantity(),
                        ci.getMenuItem().getPrice().multiply(BigDecimal.valueOf(ci.getQuantity()))
                ))
                .collect(Collectors.toList());

        BigDecimal total = itemResponses.stream()
                .map(CartItemResponse::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int totalQuantity = itemResponses.stream()
                .mapToInt(CartItemResponse::getQuantity)
                .sum();

        return new CartResponse(
                cart.getId(),
                cart.getUser().getId(),
                cart.getRestaurant() != null ? cart.getRestaurant().getId() : null,
                cart.getRestaurant() != null ? cart.getRestaurant().getName() : null,
                itemResponses,
                total,
                totalQuantity
        );
    }
}
