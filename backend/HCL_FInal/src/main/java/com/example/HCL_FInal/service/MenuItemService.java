package com.example.HCL_FInal.service;

import com.example.HCL_FInal.dto.MenuItemDtos.*;
import com.example.HCL_FInal.entity.MenuItem;
import com.example.HCL_FInal.entity.Restaurant;
import com.example.HCL_FInal.exception.ResourceNotFoundException;
import com.example.HCL_FInal.repository.MenuItemRepository;
import com.example.HCL_FInal.repository.RestaurantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class MenuItemService {
    private final MenuItemRepository menuItemRepository;
    private final RestaurantRepository restaurantRepository;

    public MenuItemService(MenuItemRepository menuItemRepository, RestaurantRepository restaurantRepository) {
        this.menuItemRepository = menuItemRepository;
        this.restaurantRepository = restaurantRepository;
    }

    public List<MenuItemResponse> listByRestaurant(Long restaurantId) {
        return menuItemRepository.findByRestaurantId(restaurantId).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    public List<MenuItemResponse> listAvailableByRestaurant(Long restaurantId) {
        return menuItemRepository.findByRestaurantIdAndAvailableTrue(restaurantId).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    public MenuItemResponse create(Long restaurantId, MenuItemRequest req) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found: " + restaurantId));
        MenuItem item = MenuItem.builder()
                .name(req.getName())
                .description(req.getDescription())
                .price(req.getPrice())
                .category(req.getCategory())
                .imageUrl(req.getImageUrl())
                .available(req.isAvailable())
                .restaurant(restaurant)
                .build();
        return toResponse(menuItemRepository.save(item));
    }

    public MenuItemResponse update(Long id, MenuItemRequest req) {
        MenuItem item = menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found: " + id));
        item.setName(req.getName());
        item.setDescription(req.getDescription());
        item.setPrice(req.getPrice());
        item.setCategory(req.getCategory());
        item.setImageUrl(req.getImageUrl());
        item.setAvailable(req.isAvailable());
        return toResponse(menuItemRepository.save(item));
    }

    public void delete(Long id) {
        if (!menuItemRepository.existsById(id)) {
            throw new ResourceNotFoundException("Menu item not found: " + id);
        }
        menuItemRepository.deleteById(id);
    }

    public MenuItem getEntity(Long id) {
        return menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found: " + id));
    }

    public MenuItemResponse toResponse(MenuItem m) {
        return new MenuItemResponse(m.getId(), m.getName(), m.getDescription(), m.getPrice(),
                m.getCategory(), m.getImageUrl(), m.isAvailable(),
                m.getRestaurant().getId(), m.getRestaurant().getName());
    }
}
