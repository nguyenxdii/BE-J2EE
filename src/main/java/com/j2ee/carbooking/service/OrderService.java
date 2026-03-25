package com.j2ee.carbooking.service;

import com.j2ee.carbooking.enums.NotificationType;
import com.j2ee.carbooking.enums.OrderStatus;
import com.j2ee.carbooking.model.Order;
import com.j2ee.carbooking.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final WalletService walletService;
    private final NotificationService notificationService;

    public OrderService(OrderRepository orderRepository, WalletService walletService, NotificationService notificationService) {
        this.orderRepository = orderRepository;
        this.walletService = walletService;
        this.notificationService = notificationService;
    }

    // Lấy tất cả đơn hàng cho Admin
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    // Lấy chi tiết đơn hàng
    public Order getOrderById(String id) {
        return orderRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));
    }

    // Cập nhật trạng thái đơn hàng (Admin)
    public Order updateStatus(String orderId, OrderStatus newStatus) {
        Order order = getOrderById(orderId);
        OrderStatus oldStatus = order.getStatus();

        if (oldStatus == OrderStatus.CANCELLED || oldStatus == OrderStatus.COMPLETED) {
            throw new RuntimeException("Không thể cập nhật trạng thái cho đơn hàng đã kết thúc");
        }

        order.setStatus(newStatus);
        orderRepository.save(order);

        // Thông báo cho user
        String message = "";
        switch (newStatus) {
            case CONFIRMED:
                message = "Đơn hàng của bạn đã được xác nhận. Vui lòng đến nhận xe đúng hẹn.";
                break;
            case RENTING:
                message = "Bạn đã bắt đầu hành trình. Chúc bạn có một chuyến đi an toàn!";
                break;
            case COMPLETED:
                message = "Đơn hàng đã hoàn thành. Cảm ơn bạn đã sử dụng dịch vụ!";
                break;
            default:
                break;
        }

        if (!message.isEmpty()) {
            notificationService.create(
                order.getUserId(),
                "Cập nhật đơn hàng",
                message,
                NotificationType.ORDER,
                order.getId()
            );
        }

        return order;
    }

    // Hủy đơn hàng và hoàn tiền (Admin)
    public void cancelOrder(String orderId, String reason) {
        Order order = getOrderById(orderId);

        if (order.getStatus() == OrderStatus.CANCELLED || order.getStatus() == OrderStatus.COMPLETED) {
            throw new RuntimeException("Đơn hàng này đã kết thúc, không thể hủy");
        }

        // Cập nhật trạng thái
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        // Hoàn tiền cọc (Nếu đã thanh toán)
        if (order.getDepositAmount() != null && order.getDepositAmount() > 0) {
            walletService.refundToWallet(order.getUserId(), order.getDepositAmount(), order.getId(), reason);
        }

        // Thông báo cho user
        notificationService.create(
            order.getUserId(),
            "Đơn hàng bị hủy",
            "Đơn hàng " + order.getOrderCode() + " đã bị hủy. Lý do: " + reason,
            NotificationType.ORDER,
            order.getId()
        );
    }
}
