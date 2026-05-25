package com.foodorder.repository;

import com.foodorder.entity.Order;
import com.foodorder.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

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

    // Eager-fetch items and their menu items in one query to avoid nested lazy
    // initialization during response building (works around Hibernate 6 issue
    // "Illegal pop() with non-matching JdbcValuesSourceProcessingState").
    @Query("SELECT DISTINCT o FROM Order o " +
            "LEFT JOIN FETCH o.orderItems oi " +
            "LEFT JOIN FETCH oi.menuItem " +
            "WHERE o.id = :id")
    Optional<Order> findByIdWithItems(@Param("id") Long id);
}
