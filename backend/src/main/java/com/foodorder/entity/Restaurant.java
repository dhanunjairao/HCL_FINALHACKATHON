package com.foodorder.entity;

import com.foodorder.enums.VerificationStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "restaurants")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Restaurant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 2000)
    private String description;

    private String cuisineType;
    private String address;
    private String phone;

    // Accepts http(s) URLs as well as base64 data: URIs, so size must be unbounded.
    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String imageUrl;

    private String proprietorName;
    private String foodLicense;
    private Double latitude;
    private Double longitude;

    private LocalTime openTime;
    private LocalTime closeTime;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;

    @Column(length = 1000)
    private String rejectionReason;

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;

    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<MenuItem> menuItems = new ArrayList<>();
}
