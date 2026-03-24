package com.j2ee.carbooking.controller;

import com.j2ee.carbooking.model.Order;
import com.j2ee.carbooking.model.Vehicle;
import com.j2ee.carbooking.repository.OrderRepository;
import com.j2ee.carbooking.repository.ReviewRepository;
import com.j2ee.carbooking.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderRepository orderRepository;
    private final VehicleRepository vehicleRepository;
    private final ReviewRepository reviewRepository;

    @GetMapping("/my-orders")
    public Map<String, Object> getMyOrders(@AuthenticationPrincipal UserDetails userDetails) {
        String userId = userDetails.getUsername();
        List<Map<String, Object>> orders = orderRepository.findByUserId(userId).stream().map(order -> {
            Vehicle vehicle = vehicleRepository.findById(order.getVehicleId()).orElse(null);
            return orderWithVehicle(order, vehicle);
        }).toList();
        return Map.of("success", true, "data", orders);
    }

    @GetMapping("/{id}")
    public Map<String, Object> getOrderById(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String id
    ) {
        String userId = userDetails.getUsername();
        Order order = orderRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));
        Vehicle vehicle = vehicleRepository.findById(order.getVehicleId()).orElse(null);
        return Map.of("success", true, "data", orderWithVehicle(order, vehicle));
    }

    private Map<String, Object> orderWithVehicle(Order order, Vehicle vehicle) {
        return Map.of(
                "id", order.getId(),
                "status", order.getStatus(),
                "startDate", order.getStartDate(),
                "endDate", order.getEndDate(),
                "depositAmount", order.getDepositAmount(),
                "vehicleId", order.getVehicleId(),
                "reviewed", reviewRepository.existsByOrderId(order.getId()),
                "vehicle", vehicle
        );
    }
}
