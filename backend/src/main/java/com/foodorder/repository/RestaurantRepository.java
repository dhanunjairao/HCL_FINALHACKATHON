package com.foodorder.repository;

import com.foodorder.entity.Restaurant;
import com.foodorder.enums.VerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
    List<Restaurant> findByActiveTrue();
    List<Restaurant> findByNameContainingIgnoreCase(String name);
    List<Restaurant> findByVerificationStatus(VerificationStatus status);
    List<Restaurant> findByVerificationStatusAndActiveTrue(VerificationStatus status);

    @Query("SELECT r FROM Restaurant r WHERE r.verificationStatus = com.foodorder.enums.VerificationStatus.APPROVED " +
            "AND r.active = true " +
            "AND (:q IS NULL OR LOWER(r.name) LIKE LOWER(CONCAT('%', :q, '%')) " +
            "     OR LOWER(r.cuisineType) LIKE LOWER(CONCAT('%', :q, '%')) " +
            "     OR LOWER(r.address) LIKE LOWER(CONCAT('%', :q, '%')))")
    List<Restaurant> search(@Param("q") String q);
}
