package com.foodorder.service;

import com.foodorder.dto.RestaurantDtos.*;
import com.foodorder.entity.Restaurant;
import com.foodorder.entity.User;
import com.foodorder.enums.VerificationStatus;
import com.foodorder.exception.BadRequestException;
import com.foodorder.exception.ResourceNotFoundException;
import com.foodorder.repository.RestaurantRepository;
import com.foodorder.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class RestaurantService {
    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;

    public RestaurantService(RestaurantRepository restaurantRepository, UserRepository userRepository) {
        this.restaurantRepository = restaurantRepository;
        this.userRepository = userRepository;
    }

    public List<RestaurantResponse> listAll() {
        return restaurantRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    /** Public listing â€” only APPROVED + active restaurants are visible to customers. */
    public List<RestaurantResponse> listPublic() {
        return restaurantRepository.findByVerificationStatusAndActiveTrue(VerificationStatus.APPROVED)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<RestaurantResponse> listPending() {
        return restaurantRepository.findByVerificationStatus(VerificationStatus.PENDING)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<RestaurantResponse> search(String q, Boolean openNow) {
        List<Restaurant> results = restaurantRepository.search(q);
        if (Boolean.TRUE.equals(openNow)) {
            LocalTime now = LocalTime.now();
            results = results.stream().filter(r -> isOpenAt(r, now)).collect(Collectors.toList());
        }
        return results.stream().map(this::toResponse).collect(Collectors.toList());
    }

    public RestaurantResponse getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    public RestaurantResponse update(Long id, RestaurantRequest req) {
        Restaurant r = findOrThrow(id);
        r.setName(req.getName());
        r.setDescription(req.getDescription());
        r.setCuisineType(req.getCuisineType());
        r.setAddress(req.getAddress());
        r.setPhone(req.getPhone());
        r.setImageUrl(req.getImageUrl());
        return toResponse(restaurantRepository.save(r));
    }

    public RestaurantResponse verify(Long id, VerificationRequest req) {
        Restaurant r = findOrThrow(id);
        if (req.getStatus() == VerificationStatus.PENDING) {
            throw new BadRequestException("Status must be APPROVED or REJECTED");
        }
        r.setVerificationStatus(req.getStatus());
        if (req.getStatus() == VerificationStatus.REJECTED) {
            r.setRejectionReason(req.getRejectionReason());
        } else {
            r.setRejectionReason(null);
        }
        Restaurant saved = restaurantRepository.save(r);

        // On approval, enable any manager linked to this restaurant so they can log in.
        // On rejection, disable them so they cannot log in until re-approved.
        boolean enable = req.getStatus() == VerificationStatus.APPROVED;
        userRepository.findAll().stream()
                .filter(u -> u.getManagedRestaurant() != null
                        && u.getManagedRestaurant().getId().equals(id)
                        && u.isEnabled() != enable)
                .forEach(u -> {
                    u.setEnabled(enable);
                    userRepository.save(u);
                });

        return toResponse(saved);
    }

    public RestaurantResponse updateTimings(Long id, TimingsRequest req) {
        Restaurant r = findOrThrow(id);
        if (req.getOpenTime().equals(req.getCloseTime())) {
            throw new BadRequestException("Open and close time cannot be the same");
        }
        r.setOpenTime(req.getOpenTime());
        r.setCloseTime(req.getCloseTime());
        return toResponse(restaurantRepository.save(r));
    }

    public void delete(Long id) {
        Restaurant r = findOrThrow(id);
        // Unlink any managers pointing at this restaurant so the FK doesn't break
        userRepository.findAll().stream()
                .filter(u -> u.getManagedRestaurant() != null && u.getManagedRestaurant().getId().equals(id))
                .forEach(u -> {
                    u.setManagedRestaurant(null);
                    userRepository.save(u);
                });
        restaurantRepository.delete(r);
    }

    private Restaurant findOrThrow(Long id) {
        return restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found: " + id));
    }

    /**
     * True if a wall-clock time falls inside [openTime, closeTime].
     * Handles overnight ranges (e.g. 22:00 â†’ 02:00).
     * If timings are missing the restaurant is treated as always open.
     */
    public static boolean isOpenAt(Restaurant r, LocalTime now) {
        if (r.getOpenTime() == null || r.getCloseTime() == null) return true;
        LocalTime open = r.getOpenTime();
        LocalTime close = r.getCloseTime();
        if (open.isBefore(close)) {
            return !now.isBefore(open) && now.isBefore(close);
        }
        return !now.isBefore(open) || now.isBefore(close);
    }

    public RestaurantResponse toResponse(Restaurant r) {
        boolean openNow = isOpenAt(r, LocalTime.now());
        RestaurantResponse resp = new RestaurantResponse();
        resp.setId(r.getId());
        resp.setName(r.getName());
        resp.setDescription(r.getDescription());
        resp.setCuisineType(r.getCuisineType());
        resp.setAddress(r.getAddress());
        resp.setPhone(r.getPhone());
        resp.setImageUrl(r.getImageUrl());
        resp.setProprietorName(r.getProprietorName());
        resp.setFoodLicense(r.getFoodLicense());
        resp.setLatitude(r.getLatitude());
        resp.setLongitude(r.getLongitude());
        resp.setOpenTime(r.getOpenTime());
        resp.setCloseTime(r.getCloseTime());
        resp.setVerificationStatus(r.getVerificationStatus());
        resp.setRejectionReason(r.getRejectionReason());
        resp.setActive(r.isActive());
        resp.setOpenNow(openNow);
        resp.setMenuItemCount(r.getMenuItems() != null ? r.getMenuItems().size() : 0);
        return resp;
    }
}
