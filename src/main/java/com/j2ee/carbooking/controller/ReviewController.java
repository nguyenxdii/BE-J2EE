package com.j2ee.carbooking.controller;

import com.j2ee.carbooking.dto.request.CreateReviewRequest;
import com.j2ee.carbooking.enums.OrderStatus;
import com.j2ee.carbooking.model.Order;
import com.j2ee.carbooking.model.Review;
import com.j2ee.carbooking.model.User;
import com.j2ee.carbooking.model.Vehicle;
import com.j2ee.carbooking.repository.OrderRepository;
import com.j2ee.carbooking.repository.ReviewRepository;
import com.j2ee.carbooking.repository.UserRepository;
import com.j2ee.carbooking.repository.VehicleRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;
    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;

    @GetMapping("/vehicle/{vehicleId}")
    public Map<String, Object> getVehicleReviews(
            @PathVariable String vehicleId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        int safePage = Math.max(0, page);
        int safeSize = Math.min(Math.max(1, size), 50);
        List<Review> reviews = reviewRepository.findByVehicleIdOrderByCreatedAtDesc(
                vehicleId, PageRequest.of(safePage, safeSize)
        );
        long total = reviewRepository.countByVehicleId(vehicleId);
        Vehicle vehicle = vehicleRepository.findById(vehicleId).orElse(null);
        List<Map<String, Object>> items = reviews.stream().map(review -> {
            User user = userRepository.findById(review.getUserId()).orElse(null);
            Map<String, Object> item = new HashMap<>();
            item.put("id", review.getId());
            item.put("userName", user != null ? user.getFullName() : "Người dùng");
            item.put("rating", review.getRating());
            item.put("comment", review.getComment() == null ? "" : review.getComment());
            item.put("createdAt", review.getCreatedAt() == null ? "" : review.getCreatedAt().format(DateTimeFormatter.ISO_DATE_TIME));
            return item;
        }).toList();

        return Map.of(
                "avgRating", vehicle != null ? vehicle.getAvgRating() : 0,
                "totalReviews", vehicle != null ? vehicle.getTotalReviews() : total,
                "items", items,
                "hasMore", (safePage + 1L) * safeSize < total
        );
    }

    @PostMapping
    public Map<String, Object> createReview(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateReviewRequest request
    ) {
        String userId = userDetails.getUsername();
        if (reviewRepository.existsByOrderId(request.getOrderId())) {
            throw new RuntimeException("Đơn hàng này đã được đánh giá");
        }
        Order order = orderRepository.findByIdAndUserId(request.getOrderId(), userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));
        if (order.getStatus() != OrderStatus.COMPLETED) {
            throw new RuntimeException("Chỉ có thể đánh giá đơn COMPLETED");
        }
        if (!order.getVehicleId().equals(request.getVehicleId())) {
            throw new RuntimeException("Thông tin xe không khớp với đơn hàng");
        }

        Review review = new Review();
        review.setOrderId(request.getOrderId());
        review.setVehicleId(request.getVehicleId());
        review.setUserId(userId);
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        reviewRepository.save(review);

        List<Review> allReviews = reviewRepository.findByVehicleId(request.getVehicleId());
        double avg = allReviews.stream().mapToInt(Review::getRating).average().orElse(0.0);
        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .orElseThrow(() -> new RuntimeException("Xe không tồn tại"));
        vehicle.setAvgRating(Math.round(avg * 10.0) / 10.0);
        vehicle.setTotalReviews(allReviews.size());
        vehicleRepository.save(vehicle);

        return Map.of("success", true, "message", "Đánh giá thành công");
    }
}
