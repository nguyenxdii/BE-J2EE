package com.j2ee.carbooking.service;

import com.j2ee.carbooking.dto.request.CreateOrderRequest;
import com.j2ee.carbooking.dto.response.OrderResponse;
import com.j2ee.carbooking.enums.*;
import com.j2ee.carbooking.model.*;
import com.j2ee.carbooking.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final MomoService momoService;
    private final NotificationService notificationService;
    private final ReviewRepository reviewRepository;
    private final WalletService walletService;

    // ----------------------------------------------------------------
    // CHỨC NĂNG 13+15: Đặt xe & Thanh toán (USER)
    // ----------------------------------------------------------------
    public OrderResponse createOrder(String userId, CreateOrderRequest request)
            throws Exception {

        // 1. Kiểm tra xe tồn tại và còn available
        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
            .orElseThrow(() -> new RuntimeException("Không tìm thấy xe"));

        if (vehicle.getStatus() == VehicleStatus.HIDDEN) {
            throw new RuntimeException("Xe này hiện không khả dụng");
        }

        // 2. Validate ngày
        if (!request.getEndDate().isAfter(request.getStartDate())) {
            throw new RuntimeException("Ngày trả xe phải sau ngày nhận xe");
        }

        if (request.getStartDate().isBefore(LocalDate.now())) {
            throw new RuntimeException("Ngày nhận xe không được trong quá khứ");
        }

        // 3. Kiểm tra xe còn trống trong khoảng ngày đó không
        boolean isBooked = orderRepository
            .existsByVehicleIdAndStatusInAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                request.getVehicleId(),
                List.of(OrderStatus.CONFIRMED, OrderStatus.RENTING),
                request.getEndDate(),
                request.getStartDate()
            );

        if (isBooked) {
            throw new RuntimeException(
                "Xe đã có người đặt trong khoảng thời gian này. Vui lòng chọn ngày khác.");
        }

        // 4. Tính tiền
        long totalDays = java.time.temporal.ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate()) + 1;
        if (totalDays <= 0) totalDays = 1;

        double rentalPrice  = vehicle.getPricePerDay() * totalDays;
        double depositAmount = vehicle.getDepositAmount();
        double totalAmount  = rentalPrice + depositAmount;

        // 5. Sinh orderCode theo format ORD-YYYYMMDD-RANDOM
        String orderCode = "ORD-"
            + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
            + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        // 6. Tạo Order object
        Order order = new Order();
        order.setOrderCode(orderCode);
        order.setUserId(userId);
        order.setVehicleId(vehicle.getId());
        order.setStartDate(request.getStartDate());
        order.setEndDate(request.getEndDate());
        order.setTotalDays((int) totalDays);
        order.setRentalPrice(rentalPrice);
        order.setDepositAmount(depositAmount);
        order.setTotalAmount(totalAmount);
        order.setStatus(OrderStatus.PENDING);
        order.setPaymentStatus(PaymentStatus.UNPAID);
        order.setPaymentMethod(request.getPaymentMethod());
        order.setNote(request.getNote());
        order.setIsTransferred(false);

        // 7. Xử lý thanh toán
        if (request.getPaymentMethod() == PaymentMethod.WALLET) {
            // --- Thanh toán bằng ví ---
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

            if (user.getWalletBalance() < depositAmount) {
                throw new RuntimeException(
                    "Số dư ví không đủ để thanh toán tiền cọc. Cần "
                    + String.format("%,.0f", depositAmount) + "đ, "
                    + "hiện có " + String.format("%,.0f", user.getWalletBalance()) + "đ");
            }

            // Trừ tiền cọc từ ví
            double balanceBefore = user.getWalletBalance();
            user.setWalletBalance(balanceBefore - depositAmount);
            userRepository.save(user);

            // Tạo WalletTransaction
            WalletTransaction tx = new WalletTransaction();
            tx.setUserId(userId);
            tx.setType(TransactionType.PAY);
            tx.setAmount(depositAmount);
            tx.setBalanceBefore(balanceBefore);
            tx.setBalanceAfter(user.getWalletBalance());
            tx.setRefType("ORDER");
            tx.setRefId(orderCode); 
            tx.setDescription("Cọc xe " + vehicle.getName() + " — " + orderCode);
            tx.setStatus(TransactionStatus.SUCCESS);
            walletTransactionRepository.save(tx);

            // Lưu order
            order.setStatus(OrderStatus.CONFIRMED); // Chuyển sang Đã xác nhận sau khi cọc
            order.setPaymentStatus(PaymentStatus.PAID);
            orderRepository.save(order);

            // Cập nhật refId trong tx sang orderId thật
            tx.setRefId(order.getId());
            walletTransactionRepository.save(tx);

            // Gửi thông báo
            notificationService.create(
                userId,
                "Đặt xe thành công",
                "Đơn hàng " + orderCode + " — xe " + vehicle.getName()
                    + " từ ngày " + request.getStartDate()
                    + " đến " + request.getEndDate() + " đã được tạo.",
                NotificationType.ORDER,
                order.getId()
            );

            // Thông báo cho Admin về đơn hàng mới
            try {
                List<User> admins = userRepository.findByRole(Role.ADMIN);
                for (User admin : admins) {
                    notificationService.create(admin.getId(), "Đơn đặt xe mới", 
                        "Khách hàng " + user.getFullName() + " vừa đặt xe " + vehicle.getName() + " (" + orderCode + ")", 
                        NotificationType.ORDER, order.getId());
                }
            } catch (Exception e) {
                System.err.println("Lỗi gửi thông báo cho Admin: " + e.getMessage());
            }

            return toResponse(order, vehicle, null);

        } else if (request.getPaymentMethod() == PaymentMethod.MOMO) {
            // --- Thanh toán bằng Momo ---
            orderRepository.save(order);

            WalletTransaction tx = new WalletTransaction();
            tx.setUserId(userId);
            tx.setType(TransactionType.PAY);
            tx.setAmount(totalAmount);
            tx.setBalanceBefore(0.0);
            tx.setBalanceAfter(0.0);
            tx.setRefType("ORDER");
            tx.setRefId("ORDER-" + order.getId());
            tx.setDescription("Đặt xe " + vehicle.getName() + "|ORDER:" + order.getId());
            tx.setStatus(TransactionStatus.PENDING);
            walletTransactionRepository.save(tx);

            String momoOrderId = "ORDER-" + order.getId()
                .substring(0, Math.min(6, order.getId().length()))
                + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

            tx.setRefId(momoOrderId);
            walletTransactionRepository.save(tx);

            double depositAmountToPay = order.getDepositAmount();
            String orderInfo = "Coc xe " + vehicle.getName() + " | " + orderCode;
            
            String payUrl = momoService.createPaymentUrl(
                order.getId(), (long) depositAmountToPay, orderInfo);
            
            return toResponse(order, vehicle, payUrl);

        } else {
            throw new RuntimeException(
                "Phương thức thanh toán không hợp lệ. Chỉ hỗ trợ WALLET hoặc MOMO");
        }
    }

    // ----------------------------------------------------------------
    // CHỨC NĂNG 16: Lịch sử đơn hàng (USER)
    // ----------------------------------------------------------------
    public List<OrderResponse> getMyOrders(String userId) {
        List<Order> orders = orderRepository.findMyHistory(userId);
        return orders.stream()
            .map(order -> {
                Vehicle vehicle = vehicleRepository
                    .findById(order.getVehicleId()).orElse(null);
                return toResponse(order, vehicle, null);
            })
            .collect(Collectors.toList());
    }

    // ----------------------------------------------------------------
    // Lấy danh sách đơn hàng của một xe (để hiện lịch bận)
    // ----------------------------------------------------------------
    public List<OrderResponse> getVehicleOrders(String vehicleId) {
        List<Order> orders = orderRepository.findByVehicleIdAndStatusIn(
            vehicleId, 
            List.of(OrderStatus.PENDING, OrderStatus.CONFIRMED, OrderStatus.RENTING)
        );
        return orders.stream()
            .map(order -> toResponse(order, null, null))
            .collect(Collectors.toList());
    }

    // ----------------------------------------------------------------
    // CHỨC NĂNG 17: Xem chi tiết đơn hàng
    // ----------------------------------------------------------------
    public OrderResponse getOrderDetail(String userId, String orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        boolean isOwner = order.getUserId().equals(userId);
        boolean isOriginalOwner = order.getOriginalUserId() != null && order.getOriginalUserId().equals(userId);
        
        if (!isOwner && !isOriginalOwner) {
            throw new RuntimeException("Bạn không có quyền xem đơn hàng này");
        }

        Vehicle vehicle = vehicleRepository
            .findById(order.getVehicleId()).orElse(null);

        return toResponse(order, vehicle, null);
    }

    // ----------------------------------------------------------------
    // CHỨC NĂNG 18: Huỷ đơn hàng (USER)
    // ----------------------------------------------------------------
    public OrderResponse cancelOrder(String userId, String orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("Bạn không có quyền huỷ đơn hàng này");
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new RuntimeException(
                "Chỉ có thể huỷ đơn hàng đang ở trạng thái PENDING. "
                + "Đơn hiện tại: " + order.getStatus());
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        Vehicle vehicle = vehicleRepository
            .findById(order.getVehicleId()).orElse(null);
        String vehicleName = vehicle != null ? vehicle.getName() : "xe";

        notificationService.create(
            userId,
            "Đơn hàng đã bị huỷ",
            "Đơn hàng " + order.getOrderCode() + " — xe " + vehicleName
                + " đã bị huỷ. Tiền cọc không được hoàn lại.",
            NotificationType.ORDER,
            order.getId()
        );

        return toResponse(order, vehicle, null);
    }

    // ----------------------------------------------------------------
    // ADMIN FUNCTIONS
    // ----------------------------------------------------------------
    
    // Lấy tất cả đơn hàng cho Admin (DTO)
    public List<OrderResponse> getAllOrdersForAdmin() {
        return orderRepository.findAllByOrderByCreatedAtDesc().stream()
            .map(o -> {
                Vehicle v = vehicleRepository.findById(o.getVehicleId()).orElse(null);
                return toResponse(o, v, null);
            })
            .collect(Collectors.toList());
    }

    // Lấy chi tiết đơn hàng (Admin)
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
    public void cancelOrderForAdmin(String orderId, String reason) {
        Order order = getOrderById(orderId);

        if (order.getStatus() == OrderStatus.CANCELLED || order.getStatus() == OrderStatus.COMPLETED) {
            throw new RuntimeException("Đơn hàng này đã kết thúc, không thể hủy");
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        if (order.getDepositAmount() != null && order.getDepositAmount() > 0 && order.getPaymentStatus() == PaymentStatus.PAID) {
            walletService.refundToWallet(order.getUserId(), order.getDepositAmount(), order.getId(), reason);
        }

        notificationService.create(
            order.getUserId(),
            "Đơn hàng bị hủy",
            "Đơn hàng " + order.getOrderCode() + " đã bị hủy. Lý do: " + reason,
            NotificationType.ORDER,
            order.getId()
        );
    }

    // ----------------------------------------------------------------
    // Momo callback handler
    // ----------------------------------------------------------------
    public void handleOrderMomoCallback(String orderId, WalletTransaction tx) {
        String description = tx.getDescription();
        int index = description.indexOf("|ORDER:");
        if (index == -1) {
            throw new RuntimeException("Không tìm thấy orderId trong description");
        }
        String realOrderId = description.substring(index + 7);

        Order order = orderRepository.findById(realOrderId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy order: " + realOrderId));

        Vehicle vehicle = vehicleRepository.findById(order.getVehicleId())
            .orElseThrow(() -> new RuntimeException("Không tìm thấy xe"));

        User user = userRepository.findById(order.getUserId())
            .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

        order.setStatus(OrderStatus.CONFIRMED);
        order.setPaymentStatus(PaymentStatus.PAID);
        orderRepository.save(order);

        tx.setBalanceBefore(user.getWalletBalance());
        tx.setBalanceAfter(user.getWalletBalance());
        tx.setStatus(TransactionStatus.SUCCESS);
        tx.setRefId(order.getId());
        walletTransactionRepository.save(tx);

        notificationService.create(
            user.getId(),
            "Đặt xe thành công",
            "Đơn hàng " + order.getOrderCode() + " — xe " + vehicle.getName()
                + " từ ngày " + order.getStartDate()
                + " đến " + order.getEndDate() + " đã được tạo.",
            NotificationType.ORDER,
            order.getId()
        );

        // Thông báo cho Admin về đơn hàng Momo thành công
        try {
            java.util.List<User> admins = userRepository.findByRole(Role.ADMIN);
            for (User admin : admins) {
                notificationService.create(admin.getId(), "Thanh toán đơn hàng thành công", 
                    "Đơn hàng " + order.getOrderCode() + " đã được thanh toán cọc qua Momo thành công.", 
                    NotificationType.ORDER, order.getId());
            }
        } catch (Exception e) {
            System.err.println("Lỗi gửi thông báo cho Admin: " + e.getMessage());
        }
    }

    // ----------------------------------------------------------------
    // Helper: map Order + Vehicle → OrderResponse
    // ----------------------------------------------------------------
    private OrderResponse toResponse(Order order, Vehicle vehicle, String payUrl) {
        OrderResponse res = new OrderResponse();

        res.setId(order.getId());
        res.setOrderCode(order.getOrderCode());

        if (vehicle != null) {
            res.setVehicleId(vehicle.getId());
            res.setVehicleName(vehicle.getName());
            res.setVehicleBrand(vehicle.getBrand());
            res.setLicensePlate(vehicle.getLicensePlate());
            res.setVehicleImage(
                vehicle.getImages() != null && !vehicle.getImages().isEmpty()
                    ? vehicle.getImages().get(0) : null
            );
        }

        res.setStartDate(order.getStartDate());
        res.setEndDate(order.getEndDate());
        res.setTotalDays(order.getTotalDays());
        res.setRentalPrice(order.getRentalPrice());
        res.setDepositAmount(order.getDepositAmount());
        res.setTotalAmount(order.getTotalAmount());
        res.setStatus(order.getStatus());
        res.setPaymentStatus(order.getPaymentStatus());
        res.setPaymentMethod(order.getPaymentMethod());
        res.setIsTransferred(order.getIsTransferred());
        res.setOriginalUserId(order.getOriginalUserId());
        res.setNote(order.getNote());
        res.setCreatedAt(order.getCreatedAt());
        res.setUpdatedAt(order.getUpdatedAt());
        res.setPayUrl(payUrl);
        res.setReviewed(reviewRepository.existsByOrderId(order.getId()));

        // Thông tin khách hàng
        res.setUserId(order.getUserId());
        userRepository.findById(order.getUserId()).ifPresent(u -> res.setUserName(u.getFullName()));

        return res;
    }
}
