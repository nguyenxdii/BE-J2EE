package com.j2ee.carbooking.service;

import com.j2ee.carbooking.dto.request.*;
import com.j2ee.carbooking.enums.*;
import com.j2ee.carbooking.model.*;
import com.j2ee.carbooking.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class WalletService {

    private final UserRepository userRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final MomoService momoService;
    private final NotificationService notificationService;
    private final DepositListingService depositListingService;
    private final DepositListingRepository depositListingRepository;
    private final OrderRepository orderRepository;
    private final VehicleRepository vehicleRepository;

    @Autowired
    public WalletService(
            UserRepository userRepository,
            WalletTransactionRepository walletTransactionRepository,
            MomoService momoService,
            NotificationService notificationService,
            @Lazy DepositListingService depositListingService,
            DepositListingRepository depositListingRepository,
            OrderRepository orderRepository,
            VehicleRepository vehicleRepository) {
        this.userRepository = userRepository;
        this.walletTransactionRepository = walletTransactionRepository;
        this.momoService = momoService;
        this.notificationService = notificationService;
        this.depositListingService = depositListingService;
        this.depositListingRepository = depositListingRepository;
        this.orderRepository = orderRepository;
        this.vehicleRepository = vehicleRepository;
    }

    // ----------------------------------------------------------------
    // CHỨC NĂNG 21: Nạp tiền vào ví — tạo payment URL Momo
    // ----------------------------------------------------------------
    public String depositViaMomo(String userId, DepositWalletRequest request)
            throws Exception {

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

        // orderId phải unique — dùng làm key tra cứu khi callback về
        // Format: WALLET-{userId6ký tự}-{random} để dễ debug
        String orderId = "WALLET-"
            + userId.substring(0, Math.min(6, userId.length()))
            + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        // Tạo WalletTransaction PENDING trước khi redirect
        WalletTransaction tx = new WalletTransaction();
        tx.setUserId(userId);
        tx.setType(TransactionType.DEPOSIT);
        tx.setAmount(request.getAmount());
        tx.setBalanceBefore(user.getWalletBalance());
        tx.setBalanceAfter(user.getWalletBalance()); // chưa cộng, cập nhật sau khi callback
        tx.setRefType("WALLET");
        tx.setRefId(orderId);
        tx.setDescription("Nạp tiền vào ví qua Momo");
        tx.setStatus(TransactionStatus.PENDING);
        walletTransactionRepository.save(tx);

        // Tạo Momo payment URL
        String orderInfo = "Nap tien vi ShopCar - " + user.getFullName();
        long amount = request.getAmount().longValue();

        return momoService.createPaymentUrl(orderId, amount, orderInfo);
    }

    // ----------------------------------------------------------------
    // CALLBACK từ Momo — gọi sau khi user thanh toán xong
    // ----------------------------------------------------------------
    public void handleMomoCallback(Map<String, String> params) {

        // 1. Xác minh chữ ký — tránh request giả mạo
        if (!momoService.verifyCallback(params)) {
            throw new RuntimeException("Chữ ký Momo không hợp lệ");
        }

        String orderId    = params.get("orderId");
        String resultCode = params.get("resultCode");

        // 2. Tìm transaction PENDING theo orderId
        WalletTransaction tx = walletTransactionRepository
            .findByRefIdAndStatus(orderId, TransactionStatus.PENDING)
            .orElseThrow(() -> new RuntimeException(
                "Không tìm thấy giao dịch pending: " + orderId));

        // 3. Thanh toán thất bại
        if (!"0".equals(resultCode)) {
            tx.setStatus(TransactionStatus.FAILED);
            walletTransactionRepository.save(tx);
            return;
        }

        // Phân biệt loại callback: nạp ví hay mua suất cọc
        if (orderId.startsWith("WALLET-")) {
            // --- Callback nạp tiền ví ---
            User user = userRepository.findById(tx.getUserId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

            double newBalance = user.getWalletBalance() + tx.getAmount();
            user.setWalletBalance(newBalance);
            userRepository.save(user);

            // 5. Cập nhật transaction → SUCCESS
            tx.setBalanceAfter(newBalance);
            tx.setStatus(TransactionStatus.SUCCESS);
            walletTransactionRepository.save(tx);

            // 6. Gửi thông báo
            notificationService.create(
                user.getId(),
                "Nạp tiền thành công",
                "Ví của bạn đã được cộng "
                    + String.format("%,.0f", tx.getAmount()) + "đ. "
                    + "Số dư hiện tại: " + String.format("%,.0f", newBalance) + "đ.",
                NotificationType.WALLET,
                tx.getId()
            );

        } else if (orderId.startsWith("DEPOSIT-")) {
            // --- Callback mua suất cọc ---
            // Parse listingId từ description: "Mua suất cọc xe ...|LISTING:{listingId}"
            String description = tx.getDescription();
            int index = description.indexOf("|LISTING:");
            if (index == -1) {
                throw new RuntimeException("Không tìm thấy listingId trong description");
            }
            String listingId = description.substring(index + 9);

            DepositListing listing = depositListingRepository.findById(listingId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy listing: " + listingId));

            Order order = orderRepository.findById(listing.getOrderId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy order"));

            Vehicle vehicle = vehicleRepository.findById(listing.getVehicleId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy xe"));

            User buyer  = userRepository.findById(tx.getUserId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy buyer"));

            User seller = userRepository.findById(listing.getSellerId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy seller"));

            double sellingPrice = listing.getSellingPrice();

            // Trừ tiền B
            double buyerBalanceBefore = buyer.getWalletBalance();
            buyer.setWalletBalance(buyerBalanceBefore - sellingPrice);
            userRepository.save(buyer);

            tx.setBalanceBefore(buyerBalanceBefore);
            tx.setBalanceAfter(buyer.getWalletBalance());
            tx.setStatus(TransactionStatus.SUCCESS);
            walletTransactionRepository.save(tx);

            // Cộng tiền A
            double sellerBalanceBefore = seller.getWalletBalance();
            seller.setWalletBalance(sellerBalanceBefore + sellingPrice);
            userRepository.save(seller);

            WalletTransaction txReceive = new WalletTransaction();
            txReceive.setUserId(seller.getId());
            txReceive.setType(TransactionType.RECEIVE);
            txReceive.setAmount(sellingPrice);
            txReceive.setBalanceBefore(sellerBalanceBefore);
            txReceive.setBalanceAfter(seller.getWalletBalance());
            txReceive.setRefType("DEPOSIT_LISTING");
            txReceive.setRefId(listing.getId());
            txReceive.setDescription("Nhận tiền bán suất cọc xe " + vehicle.getName());
            txReceive.setStatus(TransactionStatus.SUCCESS);
            walletTransactionRepository.save(txReceive);

            // Hoàn tất sang nhượng
            depositListingService.completeListing(listing, order, buyer, seller, vehicle);
        }
    }

    // ----------------------------------------------------------------
    // CHỨC NĂNG 22: Lịch sử giao dịch ví
    // ----------------------------------------------------------------
    public List<WalletTransaction> getTransactionHistory(String userId) {
        return walletTransactionRepository
            .findByUserIdOrderByCreatedAtDesc(userId);
    }
}
