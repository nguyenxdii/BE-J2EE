package com.j2ee.carbooking.controller;

import com.j2ee.carbooking.dto.response.AppApiResponse;
import com.j2ee.carbooking.enums.OrderStatus;
import com.j2ee.carbooking.model.Order;
import com.j2ee.carbooking.service.OrderService;
import com.j2ee.carbooking.repository.UserRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/orders")
public class AdminOrderController {

    private final OrderService orderService;
    private final com.j2ee.carbooking.service.MailService mailService;
    private final UserRepository userRepository;

    public AdminOrderController(OrderService orderService, com.j2ee.carbooking.service.MailService mailService, UserRepository userRepository) {
        this.orderService = orderService;
        this.mailService = mailService;
        this.userRepository = userRepository;
    }

    // GET /api/admin/orders — xem tất cả đơn
    @GetMapping
    public ResponseEntity<AppApiResponse<List<Order>>> getAllOrders() {
        return ResponseEntity.ok(AppApiResponse.success("Lấy danh sách đơn hàng thành công", orderService.getAllOrders()));
    }

    // PUT /api/admin/orders/{id}/status — cập nhật trạng thái (CONFIRMED / RENTING / COMPLETED)
    @PutMapping("/{id}/status")
    public ResponseEntity<AppApiResponse<Order>> updateStatus(
            @PathVariable String id,
            @RequestBody Map<String, String> body) {
        
        String statusStr = body.get("status");
        OrderStatus status = OrderStatus.valueOf(statusStr.toUpperCase());
        Order updated = orderService.updateStatus(id, status);
        
        return ResponseEntity.ok(AppApiResponse.success("Cập nhật trạng thái thành công", updated));
    }

    // POST /api/admin/orders/{id}/cancel — huỷ + hoàn tiền ví
    @PostMapping("/{id}/cancel")
    public ResponseEntity<AppApiResponse<String>> cancelOrder(
            @PathVariable String id,
            @RequestBody Map<String, String> body) {
        
        String reason = body.getOrDefault("reason", "Admin hủy đơn");
        Order order = orderService.getOrderById(id);
        orderService.cancelOrder(id, reason);
        
        // Gửi email
        userRepository.findById(order.getUserId()).ifPresent(u -> {
            mailService.sendOrderCancelEmail(u.getEmail(), order.getOrderCode(), reason);
        });
        
        return ResponseEntity.ok(AppApiResponse.success("Đã hủy đơn hàng và hoàn tiền thành công"));
    }
}
