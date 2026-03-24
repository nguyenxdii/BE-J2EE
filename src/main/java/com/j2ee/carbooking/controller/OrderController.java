package com.j2ee.carbooking.controller;

import com.j2ee.carbooking.dto.request.CreateOrderRequest;
import com.j2ee.carbooking.dto.response.ApiResponse;
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
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateOrderRequest request) throws Exception {

        OrderResponse data = orderService.createOrder(
            userDetails.getUsername(), request);

        return ResponseEntity.ok(
            ApiResponse.success("Đặt xe thành công", data));
    }

    // GET /api/orders/my — lịch sử đơn hàng (cần đăng nhập)
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getMyOrders(
            @AuthenticationPrincipal UserDetails userDetails) {

        List<OrderResponse> data =
            orderService.getMyOrders(userDetails.getUsername());

        return ResponseEntity.ok(
            ApiResponse.success("Lịch sử đơn hàng", data));
    }

    // GET /api/orders/{id} — chi tiết đơn hàng (cần đăng nhập)
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderDetail(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String id) {

        OrderResponse data = orderService.getOrderDetail(
            userDetails.getUsername(), id);

        return ResponseEntity.ok(
            ApiResponse.success("Chi tiết đơn hàng", data));
    }

    // PUT /api/orders/{id}/cancel — huỷ đơn (cần đăng nhập)
    @PutMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String id) {

        OrderResponse data = orderService.cancelOrder(
            userDetails.getUsername(), id);

        return ResponseEntity.ok(
            ApiResponse.success("Đã huỷ đơn hàng", data));
    }
}
