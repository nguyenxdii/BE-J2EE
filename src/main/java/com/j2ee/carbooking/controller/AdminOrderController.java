package com.j2ee.carbooking.controller;

import com.j2ee.carbooking.dto.response.AppApiResponse;
import com.j2ee.carbooking.enums.OrderStatus;
import com.j2ee.carbooking.model.Order;
import com.j2ee.carbooking.service.OrderService;
import com.j2ee.carbooking.service.EmailService;
import com.j2ee.carbooking.repository.UserRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/orders")
public class AdminOrderController {

    private final OrderService orderService;
    private final EmailService emailService;
    private final UserRepository userRepository;

    public AdminOrderController(OrderService orderService, EmailService emailService, UserRepository userRepository) {
        this.orderService = orderService;
        this.emailService = emailService;
        this.userRepository = userRepository;
    }

    // GET /api/admin/orders — xem tất cả đơn
    @GetMapping
    public ResponseEntity<AppApiResponse<List<com.j2ee.carbooking.dto.response.OrderResponse>>> getAllOrders() {
        return ResponseEntity.ok(AppApiResponse.success("Lấy danh sách đơn hàng thành công", orderService.getAllOrdersForAdmin()));
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
        orderService.cancelOrderForAdmin(id, reason);
        
        // Gửi email
        userRepository.findById(order.getUserId()).ifPresent(u -> {
            emailService.sendEmail(u.getEmail(), "Thông báo hủy đơn hàng", 
            "Chào bạn,\n\nĐơn hàng của bạn với mã " + order.getOrderCode() + " đã bị Admin hủy bỏ vì lý do: " + reason + ".\n\nTrân trọng!");
        });
        
        return ResponseEntity.ok(AppApiResponse.success("Đã hủy đơn hàng và hoàn tiền thành công"));
    }
}
