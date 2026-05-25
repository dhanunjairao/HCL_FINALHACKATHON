package com.example.HCL_FInal.config;

import com.example.HCL_FInal.entity.*;
import com.example.HCL_FInal.enums.Role;
import com.example.HCL_FInal.repository.CartRepository;
import com.example.HCL_FInal.repository.MenuItemRepository;
import com.example.HCL_FInal.repository.RestaurantRepository;
import com.example.HCL_FInal.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

// @Configuration  // Temporarily disabled: seeder will not run on startup
public class DataSeeder {

    private static final Logger logger = LoggerFactory.getLogger(DataSeeder.class);

    @Bean
    public CommandLineRunner seed(UserRepository userRepo, RestaurantRepository restaurantRepo,
                                  MenuItemRepository menuRepo, CartRepository cartRepo,
                                  PasswordEncoder encoder) {
        return args -> {
            if (userRepo.count() > 0) {
                logger.info("Data already seeded, skipping.");
                return;
            }

            // Default admin
            User admin = User.builder()
                    .username("admin")
                    .email("admin@foodorder.com")
                    .password(encoder.encode("admin123"))
                    .fullName("System Admin")
                    .role(Role.ADMIN)
                    .enabled(true)
                    .build();
            userRepo.save(admin);

            // Default customer
            User customer = User.builder()
                    .username("john")
                    .email("john@example.com")
                    .password(encoder.encode("john123"))
                    .fullName("John Doe")
                    .phone("9876543210")
                    .address("123 Main St, City")
                    .role(Role.USER)
                    .enabled(true)
                    .build();
            customer = userRepo.save(customer);

            // Create an empty cart for the customer
            cartRepo.save(Cart.builder()
                    .user(customer)
                    .createdAt(LocalDateTime.now())
                    .build());

            // Sample restaurants
            Restaurant r1 = restaurantRepo.save(Restaurant.builder()
                    .name("Pizza Palace")
                    .description("Authentic Italian pizzas baked fresh")
                    .cuisineType("Italian")
                    .address("45 Park Avenue")
                    .phone("9001234567")
                    .imageUrl("https://images.unsplash.com/photo-1513104890138-7c749659a591?w=400")
                    .active(true).build());

            Restaurant r2 = restaurantRepo.save(Restaurant.builder()
                    .name("Burger Hub")
                    .description("Juicy burgers and crispy fries")
                    .cuisineType("American")
                    .address("78 Market Street")
                    .phone("9002345678")
                    .imageUrl("https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=400")
                    .active(true).build());

            Restaurant r3 = restaurantRepo.save(Restaurant.builder()
                    .name("Spice Garden")
                    .description("Authentic Indian flavors")
                    .cuisineType("Indian")
                    .address("12 Brigade Road")
                    .phone("9003456789")
                    .imageUrl("https://images.unsplash.com/photo-1565557623262-b51c2513a641?w=400")
                    .active(true).build());

            // Sample managers, one for each restaurant
            User m1 = User.builder().username("manager1").email("m1@foodorder.com")
                    .password(encoder.encode("manager123")).fullName("Pizza Manager")
                    .role(Role.MANAGER).managedRestaurant(r1).enabled(true).build();
            User m2 = User.builder().username("manager2").email("m2@foodorder.com")
                    .password(encoder.encode("manager123")).fullName("Burger Manager")
                    .role(Role.MANAGER).managedRestaurant(r2).enabled(true).build();
            User m3 = User.builder().username("manager3").email("m3@foodorder.com")
                    .password(encoder.encode("manager123")).fullName("Spice Manager")
                    .role(Role.MANAGER).managedRestaurant(r3).enabled(true).build();
            userRepo.saveAll(List.of(m1, m2, m3));

            // Pizza Palace menu
            menuRepo.saveAll(List.of(
                    MenuItem.builder().name("Margherita Pizza").description("Classic tomato, mozzarella, basil")
                            .price(new BigDecimal("299")).category("Pizza")
                            .imageUrl("https://images.unsplash.com/photo-1604068549290-dea0e4a305ca?w=300")
                            .available(true).restaurant(r1).build(),
                    MenuItem.builder().name("Pepperoni Pizza").description("Loaded with pepperoni")
                            .price(new BigDecimal("399")).category("Pizza")
                            .imageUrl("https://images.unsplash.com/photo-1628840042765-356cda07504e?w=300")
                            .available(true).restaurant(r1).build(),
                    MenuItem.builder().name("Garlic Bread").description("Toasted bread with garlic butter")
                            .price(new BigDecimal("149")).category("Sides")
                            .imageUrl("https://images.unsplash.com/photo-1573140247632-f8fd74997d5c?w=300")
                            .available(true).restaurant(r1).build(),
                    MenuItem.builder().name("Coke").description("Chilled 500ml")
                            .price(new BigDecimal("60")).category("Beverages")
                            .imageUrl("https://images.unsplash.com/photo-1554866585-cd94860890b7?w=300")
                            .available(true).restaurant(r1).build()
            ));

            // Burger Hub menu
            menuRepo.saveAll(List.of(
                    MenuItem.builder().name("Classic Cheeseburger").description("Beef patty, cheese, lettuce")
                            .price(new BigDecimal("249")).category("Burgers")
                            .imageUrl("https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=300")
                            .available(true).restaurant(r2).build(),
                    MenuItem.builder().name("Chicken Burger").description("Crispy chicken patty")
                            .price(new BigDecimal("229")).category("Burgers")
                            .imageUrl("https://images.unsplash.com/photo-1606131731446-5568d87113aa?w=300")
                            .available(true).restaurant(r2).build(),
                    MenuItem.builder().name("French Fries").description("Crispy golden fries")
                            .price(new BigDecimal("99")).category("Sides")
                            .imageUrl("https://images.unsplash.com/photo-1573080496219-bb080dd4f877?w=300")
                            .available(true).restaurant(r2).build(),
                    MenuItem.builder().name("Chocolate Shake").description("Thick chocolate milkshake")
                            .price(new BigDecimal("149")).category("Beverages")
                            .imageUrl("https://images.unsplash.com/photo-1572490122747-3968b75cc699?w=300")
                            .available(true).restaurant(r2).build()
            ));

            // Spice Garden menu
            menuRepo.saveAll(List.of(
                    MenuItem.builder().name("Butter Chicken").description("Creamy tomato chicken curry")
                            .price(new BigDecimal("329")).category("Main Course")
                            .imageUrl("https://images.unsplash.com/photo-1603894584373-5ac82b2ae398?w=300")
                            .available(true).restaurant(r3).build(),
                    MenuItem.builder().name("Paneer Tikka").description("Grilled cottage cheese")
                            .price(new BigDecimal("279")).category("Starters")
                            .imageUrl("https://images.unsplash.com/photo-1567188040759-fb8a883dc6d8?w=300")
                            .available(true).restaurant(r3).build(),
                    MenuItem.builder().name("Garlic Naan").description("Tandoor-baked bread")
                            .price(new BigDecimal("59")).category("Breads")
                            .imageUrl("https://images.unsplash.com/photo-1601050690597-df0568f70950?w=300")
                            .available(true).restaurant(r3).build(),
                    MenuItem.builder().name("Mango Lassi").description("Sweet yogurt drink")
                            .price(new BigDecimal("89")).category("Beverages")
                            .imageUrl("https://images.unsplash.com/photo-1626203049284-c3e6c8b5d3a3?w=300")
                            .available(true).restaurant(r3).build()
            ));

            logger.info("=== Sample Data Loaded ===");
            logger.info("ADMIN   -> username: admin    | password: admin123");
            logger.info("MANAGER -> username: manager1 | password: manager123 (Pizza Palace)");
            logger.info("MANAGER -> username: manager2 | password: manager123 (Burger Hub)");
            logger.info("MANAGER -> username: manager3 | password: manager123 (Spice Garden)");
            logger.info("USER    -> username: john     | password: john123");
        };
    }
}
