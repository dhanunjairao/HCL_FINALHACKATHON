package com.foodorder.service;

import com.foodorder.dto.OrderDtos.*;
import com.foodorder.entity.*;
import com.foodorder.enums.OrderStatus;
import com.foodorder.enums.PaymentMethod;
import com.foodorder.exception.BadRequestException;
import com.foodorder.exception.ResourceNotFoundException;
import com.foodorder.repository.MenuItemRepository;
import com.foodorder.repository.OrderRepository;
import com.foodorder.repository.RestaurantRepository;
import com.foodorder.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;
    private final EmailService emailService;
    private final CartService cartService;

    public OrderService(OrderRepository orderRepository, UserRepository userRepository,
                        RestaurantRepository restaurantRepository, MenuItemRepository menuItemRepository,
                        EmailService emailService, CartService cartService) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.restaurantRepository = restaurantRepository;
        this.menuItemRepository = menuItemRepository;
        this.emailService = emailService;
        this.cartService = cartService;
    }

    public OrderResponse placeOrder(String username, OrderRequest req) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Two flows are supported. If items are supplied in the request body we
        // use those. Otherwise we read the user's persistent cart.
        List<OrderItemRequest> itemRequests;
        Long restaurantId;

        if (req.getItems() != null && !req.getItems().isEmpty()) {
            itemRequests = req.getItems();
            restaurantId = req.getRestaurantId();
            if (restaurantId == null) {
                throw new BadRequestException("restaurantId is required when items are sent directly");
            }
        } else {
            Cart cart = cartService.getOrCreateCart(username);
            if (cart.getItems().isEmpty()) {
                throw new BadRequestException("Cart is empty");
            }
            restaurantId = cart.getRestaurant().getId();
            itemRequests = cart.getItems().stream()
                    .map(ci -> new OrderItemRequest(ci.getMenuItem().getId(), ci.getQuantity()))
                    .collect(Collectors.toList());
        }

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));

        Order order = Order.builder()
                .user(user)
                .restaurant(restaurant)
                .status(OrderStatus.PENDING)
                .deliveryAddress(req.getDeliveryAddress() != null ? req.getDeliveryAddress() : user.getAddress())
                .phone(req.getPhone() != null ? req.getPhone() : user.getPhone())
                .notes(req.getNotes())
                .paymentMethod(req.getPaymentMethod() != null ? req.getPaymentMethod() : PaymentMethod.CASH_ON_DELIVERY)
                .orderDate(LocalDateTime.now())
                .build();

        List<OrderItem> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (OrderItemRequest itemReq : itemRequests) {
            MenuItem menuItem = menuItemRepository.findById(itemReq.getMenuItemId())
                    .orElseThrow(() -> new ResourceNotFoundException("Menu item not found: " + itemReq.getMenuItemId()));
            if (!menuItem.isAvailable()) {
                throw new BadRequestException("Item not available: " + menuItem.getName());
            }
            if (!menuItem.getRestaurant().getId().equals(restaurant.getId())) {
                throw new BadRequestException("Menu item does not belong to selected restaurant");
            }
            OrderItem oi = OrderItem.builder()
                    .order(order)
                    .menuItem(menuItem)
                    .quantity(itemReq.getQuantity())
                    .price(menuItem.getPrice())
                    .build();
            items.add(oi);
            total = total.add(menuItem.getPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity())));
        }
        order.setOrderItems(items);
        order.setTotalAmount(total);

        Order saved = orderRepository.save(order);
        logger.info("Order placed: id={}, user={}, total={}", saved.getId(), username, total);

        // Clear the persistent cart now that order is placed
        cartService.clearCartForUser(user.getId());

        try {
            emailService.sendOrderConfirmation(
                    saved.getUser().getEmail(),
                    saved.getUser().getUsername(),
                    saved.getId(),
                    saved.getTotalAmount(),
                    saved.getStatus());
        } catch (Exception ignored) {}
        return toResponse(saved);
    }

    public List<OrderResponse> getOrdersForUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return orderRepository.findByUserIdOrderByOrderDateDesc(user.getId()).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    public List<OrderResponse> getOrdersForRestaurant(Long restaurantId) {
        return orderRepository.findByRestaurantIdOrderByOrderDateDesc(restaurantId).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAllByOrderByOrderDateDesc().stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    public OrderResponse getById(Long id) {
        Order order = orderRepository.findByIdWithItems(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + id));
        return toResponse(order);
    }

    public OrderResponse updateStatus(Long id, OrderStatus newStatus, Long managerRestaurantId) {
        Order order = orderRepository.findByIdWithItems(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + id));

        // If invoked by a manager, ensure they own the restaurant for this order
        if (managerRestaurantId != null && !order.getRestaurant().getId().equals(managerRestaurantId)) {
            throw new BadRequestException("You can only manage orders for your own restaurant");
        }
        order.setStatus(newStatus);
        order.setUpdatedAt(LocalDateTime.now());
        Order saved = orderRepository.save(order);
        logger.info("Order {} status updated to {}", id, newStatus);
        try {
            emailService.sendStatusUpdate(
                    saved.getUser().getEmail(),
                    saved.getUser().getUsername(),
                    saved.getId(),
                    saved.getStatus());
        } catch (Exception ignored) {}
        return toResponse(saved);
    }

    /** Orders that are READY for pickup and not yet claimed by any delivery person. */
    public List<OrderResponse> listAvailableForDelivery() {
        return orderRepository.findByStatusAndDeliveryPersonIsNullOrderByOrderDateAsc(OrderStatus.READY).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    /** All orders assigned to a given delivery person (active + history). */
    public List<OrderResponse> listForDeliveryPerson(String username) {
        User dp = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return orderRepository.findByDeliveryPersonIdOrderByOrderDateDesc(dp.getId()).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    /** Delivery person claims a READY order. Atomic check prevents double-claim. */
    public OrderResponse claimDelivery(Long id, String username) {
        User dp = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Order order = orderRepository.findByIdWithItems(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + id));
        if (order.getStatus() != OrderStatus.READY) {
            throw new BadRequestException("Only READY orders can be claimed for delivery");
        }
        if (order.getDeliveryPerson() != null) {
            throw new BadRequestException("This order has already been claimed");
        }
        order.setDeliveryPerson(dp);
        order.setStatus(OrderStatus.OUT_FOR_DELIVERY);
        order.setUpdatedAt(LocalDateTime.now());
        Order saved = orderRepository.save(order);
        logger.info("Order {} claimed for delivery by {}", id, username);
        try {
            emailService.sendStatusUpdate(
                    saved.getUser().getEmail(),
                    saved.getUser().getUsername(),
                    saved.getId(),
                    saved.getStatus());
        } catch (Exception ignored) {}
        return toResponse(saved);
    }

    /** Delivery person marks their assigned order as DELIVERED. */
    public OrderResponse markDelivered(Long id, String username) {
        Order order = orderRepository.findByIdWithItems(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + id));
        if (order.getDeliveryPerson() == null || !order.getDeliveryPerson().getUsername().equals(username)) {
            throw new BadRequestException("You can only mark your own deliveries as delivered");
        }
        if (order.getStatus() != OrderStatus.OUT_FOR_DELIVERY) {
            throw new BadRequestException("Only OUT_FOR_DELIVERY orders can be marked as delivered");
        }
        order.setStatus(OrderStatus.DELIVERED);
        order.setUpdatedAt(LocalDateTime.now());
        Order saved = orderRepository.save(order);
        logger.info("Order {} marked DELIVERED by {}", id, username);
        try {
            emailService.sendStatusUpdate(
                    saved.getUser().getEmail(),
                    saved.getUser().getUsername(),
                    saved.getId(),
                    saved.getStatus());
        } catch (Exception ignored) {}
        return toResponse(saved);
    }

    public OrderResponse cancelOrder(Long id, String username) {
        Order order = orderRepository.findByIdWithItems(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + id));
        if (!order.getUser().getUsername().equals(username)) {
            throw new BadRequestException("You can only cancel your own orders");
        }
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BadRequestException("Only PENDING orders can be cancelled");
        }
        order.setStatus(OrderStatus.CANCELLED);
        order.setUpdatedAt(LocalDateTime.now());
        return toResponse(orderRepository.save(order));
    }

    private OrderResponse toResponse(Order order) {
        List<OrderItemResponse> items = order.getOrderItems().stream().map(oi -> new OrderItemResponse(
                oi.getId(),
                oi.getMenuItem().getId(),
                oi.getMenuItem().getName(),
                oi.getQuantity(),
                oi.getPrice(),
                oi.getPrice().multiply(BigDecimal.valueOf(oi.getQuantity()))
        )).collect(Collectors.toList());

        User dp = order.getDeliveryPerson();
        return new OrderResponse(
                order.getId(),
                order.getUser().getId(),
                order.getUser().getUsername(),
                order.getUser().getFullName(),
                order.getRestaurant().getId(),
                order.getRestaurant().getName(),
                items,
                order.getTotalAmount(),
                order.getStatus(),
                order.getDeliveryAddress(),
                order.getPhone(),
                order.getNotes(),
                order.getPaymentMethod(),
                dp != null ? dp.getId() : null,
                dp != null ? (dp.getFullName() != null ? dp.getFullName() : dp.getUsername()) : null,
                order.getOrderDate(),
                order.getUpdatedAt()
        );
    }
}
