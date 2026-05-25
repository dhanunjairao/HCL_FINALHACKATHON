package com.example.HCL_FInal.repository;

import com.example.HCL_FInal.entity.Order;
import com.example.HCL_FInal.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserIdOrderByOrderDateDesc(Long userId);
    List<Order> findByRestaurantIdOrderByOrderDateDesc(Long restaurantId);
    List<Order> findByRestaurantIdAndStatusOrderByOrderDateDesc(Long restaurantId, OrderStatus status);
    List<Order> findAllByOrderByOrderDateDesc();

    // Delivery queue: orders ready to be picked up and not yet claimed by anyone.
    List<Order> findByStatusAndDeliveryPersonIsNullOrderByOrderDateAsc(OrderStatus status);

    // Orders assigned to a specific delivery person (current + history).
    List<Order> findByDeliveryPersonIdOrderByOrderDateDesc(Long deliveryPersonId);
}
