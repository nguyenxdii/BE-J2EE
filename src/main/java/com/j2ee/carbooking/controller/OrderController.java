package com.j2ee.carbooking.controller;

import com.j2ee.carbooking.dto.request.CreateOrderRequest;
import com.j2ee.carbooking.dto.response.AppApiResponse;
import com.j2ee.carbooking.dto.response.OrderResponse;
import com.j2ee.carbooking.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // POST /api/orders — đặt xe (cần đăng nhập)
    @PostMapping
    public ResponseEntity<AppApiResponse<OrderResponse>> createOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateOrderRequest request) throws Exception {

        OrderResponse data = orderService.createOrder(
            userDetails.getUsername(), request);

        return ResponseEntity.ok(
            AppApiResponse.success("Đặt xe thành công", data));
    }

    // GET /api/orders/my — lịch sử đơn hàng (cần đăng nhập)
    @GetMapping("/my")
    public ResponseEntity<AppApiResponse<List<OrderResponse>>> getMyOrders(
            @AuthenticationPrincipal UserDetails userDetails) {

        List<OrderResponse> data =
            orderService.getMyOrders(userDetails.getUsername());

        return ResponseEntity.ok(
            AppApiResponse.success("Lịch sử đơn hàng", data));
    }

    // GET /api/orders/{id} — chi tiết đơn hàng (cần đăng nhập)
    @GetMapping("/{id}")
    public ResponseEntity<AppApiResponse<OrderResponse>> getOrderDetail(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String id) {

        OrderResponse data = orderService.getOrderDetail(
            userDetails.getUsername(), id);

        return ResponseEntity.ok(
            AppApiResponse.success("Chi tiết đơn hàng", data));
    }

    // PUT /api/orders/{id}/cancel — huỷ đơn (cần đăng nhập)
    @PutMapping("/{id}/cancel")
    public ResponseEntity<AppApiResponse<OrderResponse>> cancelOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String id) {

        OrderResponse data = orderService.cancelOrder(
            userDetails.getUsername(), id);

        return ResponseEntity.ok(
            AppApiResponse.success("Đã huỷ đơn hàng", data));
    }

    // GET /api/orders/vehicle/{vehicleId} — lấy lịch bận của xe
    @GetMapping("/vehicle/{vehicleId}")
    public ResponseEntity<AppApiResponse<List<OrderResponse>>> getVehicleOrders(
            @PathVariable String vehicleId) {

        List<OrderResponse> data = orderService.getVehicleOrders(vehicleId);

        return ResponseEntity.ok(
            AppApiResponse.success("Lịch bận của xe", data));
    }
}
