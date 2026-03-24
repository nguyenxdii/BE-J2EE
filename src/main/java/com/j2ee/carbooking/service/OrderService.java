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

    // ----------------------------------------------------------------
    // CHỨC NĂNG 13+15: Đặt xe & Thanh toán
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
        long totalDays = request.getStartDate().until(request.getEndDate()).getDays();
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

            return toResponse(order, vehicle, null);

        } else if (request.getPaymentMethod() == PaymentMethod.MOMO) {
            // --- Thanh toán bằng Momo ---
            // Lưu order UNPAID trước
            orderRepository.save(order);

            // Tạo WalletTransaction PENDING
            WalletTransaction tx = new WalletTransaction();
            tx.setUserId(userId);
            tx.setType(TransactionType.PAY);
            tx.setAmount(totalAmount);
            tx.setBalanceBefore(0.0);
            tx.setBalanceAfter(0.0);
            tx.setRefType("ORDER");
            tx.setRefId("ORDER-" + order.getId()); // prefix ORDER- để phân biệt callback
            tx.setDescription("Đặt xe " + vehicle.getName() + "|ORDER:" + order.getId());
            tx.setStatus(TransactionStatus.PENDING);
            walletTransactionRepository.save(tx);

            String momoOrderId = "ORDER-" + order.getId()
                .substring(0, Math.min(6, order.getId().length()))
                + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

            // Cập nhật refId sang momoOrderId
            tx.setRefId(momoOrderId);
            tx.setDescription("Cọc xe " + vehicle.getName() + "|ORDER:" + order.getId()); // Updated description
            walletTransactionRepository.save(tx);

            // Thu tiền cọc qua Momo
            double depositAmountToPay = order.getDepositAmount(); // Use a different variable name to avoid confusion
            String orderInfo = "Coc xe " + vehicle.getName() + " | " + orderCode;
            
            String payUrl = momoService.createPaymentUrl(
                order.getId(), (long) depositAmountToPay, orderInfo); // Changed momoOrderId to order.getId() and totalAmount to depositAmountToPay
            
            return toResponse(order, vehicle, payUrl);

        } else {
            throw new RuntimeException(
                "Phương thức thanh toán không hợp lệ. Chỉ hỗ trợ WALLET hoặc MOMO");
        }
    }

    // ----------------------------------------------------------------
    // CHỨC NĂNG 16: Lịch sử đơn hàng
    // ----------------------------------------------------------------
    public List<OrderResponse> getMyOrders(String userId) {
        List<Order> orders = orderRepository.findByUserId(userId);
        return orders.stream()
            .map(order -> {
                Vehicle vehicle = vehicleRepository
                    .findById(order.getVehicleId()).orElse(null);
                return toResponse(order, vehicle, null);
            })
            .collect(Collectors.toList());
    }

    // ----------------------------------------------------------------
    // CHỨC NĂNG 17: Xem chi tiết đơn hàng
    // ----------------------------------------------------------------
    public OrderResponse getOrderDetail(String userId, String orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        // Chỉ chủ đơn mới xem được
        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("Bạn không có quyền xem đơn hàng này");
        }

        Vehicle vehicle = vehicleRepository
            .findById(order.getVehicleId()).orElse(null);

        return toResponse(order, vehicle, null);
    }

    // ----------------------------------------------------------------
    // CHỨC NĂNG 18: Huỷ đơn hàng
    // ----------------------------------------------------------------
    public OrderResponse cancelOrder(String userId, String orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        // 1. Chỉ chủ đơn mới huỷ được
        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("Bạn không có quyền huỷ đơn hàng này");
        }

        // 2. Chỉ huỷ được khi PENDING
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new RuntimeException(
                "Chỉ có thể huỷ đơn hàng đang ở trạng thái PENDING. "
                + "Đơn hiện tại: " + order.getStatus());
        }

        // 3. Đổi status → CANCELLED
        // Lưu ý: tiền cọc KHÔNG hoàn lại khi user tự huỷ
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        Vehicle vehicle = vehicleRepository
            .findById(order.getVehicleId()).orElse(null);
        String vehicleName = vehicle != null ? vehicle.getName() : "xe";

        // 4. Gửi thông báo
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
    // Xử lý Momo callback cho đặt xe
    // Gọi từ WalletService.handleMomoCallback khi orderId starts with "ORDER-"
    // ----------------------------------------------------------------
    public void handleOrderMomoCallback(String orderId, WalletTransaction tx) {
        // Parse orderId từ description: "Đặt xe ...|ORDER:{orderId}"
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

        // Cập nhật order → CONFIRMED + PAID
        order.setStatus(OrderStatus.CONFIRMED);
        order.setPaymentStatus(PaymentStatus.PAID);
        orderRepository.save(order);

        // Cập nhật tx
        tx.setBalanceBefore(user.getWalletBalance());
        tx.setBalanceAfter(user.getWalletBalance()); // Momo — không trừ ví
        tx.setStatus(TransactionStatus.SUCCESS);
        tx.setRefId(order.getId());
        walletTransactionRepository.save(tx);

        // Gửi thông báo
        notificationService.create(
            user.getId(),
            "Đặt xe thành công",
            "Đơn hàng " + order.getOrderCode() + " — xe " + vehicle.getName()
                + " từ ngày " + order.getStartDate()
                + " đến " + order.getEndDate() + " đã được tạo.",
            NotificationType.ORDER,
            order.getId()
        );
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

        return res;
    }
}
