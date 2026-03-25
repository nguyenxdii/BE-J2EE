package com.j2ee.carbooking.service;

import com.j2ee.carbooking.dto.request.*;
import com.j2ee.carbooking.dto.response.*;
import com.j2ee.carbooking.enums.*;
import com.j2ee.carbooking.model.*;
import com.j2ee.carbooking.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DepositListingService {

    private final DepositListingRepository depositListingRepository;
    private final OrderRepository orderRepository;
    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final MomoService momoService;
    private final NotificationService notificationService;

    // Tỉ lệ người bán nhận được — 60% tiền cọc gốc
    private static final double SELLER_RATIO = 0.6;

    // ----------------------------------------------------------------
    // CHỨC NĂNG 23: Đăng bán suất cọc
    // ----------------------------------------------------------------
    public DepositListingResponse createListing(String userId,
                                                CreateDepositListingRequest request) {

        // 1. Lấy đơn hàng, kiểm tra tồn tại
        Order order = orderRepository.findById(request.getOrderId())
            .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        // 2. Đơn phải thuộc về user đang request
        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("Bạn không có quyền đăng bán đơn hàng này");
        }

        // 3. Chỉ đăng được khi đơn đang PENDING hoặc CONFIRMED
        if (order.getStatus() != OrderStatus.PENDING
                && order.getStatus() != OrderStatus.CONFIRMED) {
            throw new RuntimeException(
                "Chỉ có thể đăng bán suất cọc khi đơn đang PENDING hoặc CONFIRMED");
        }

        // 4. startDate phải còn hơn 24 tiếng nữa
        LocalDateTime deadline = order.getStartDate()
            .atStartOfDay()
            .minusHours(24);
        if (LocalDateTime.now().isAfter(deadline)) {
            throw new RuntimeException(
                "Đã quá thời hạn đăng bán — cần đăng trước khi nhận xe ít nhất 24 tiếng");
        }

        // 5. Không cho phép bán lại suất cọc đã mua (Chỉ chủ gốc mới được bán)
        if (order.getIsTransferred()) {
            throw new RuntimeException("Suất cọc đã sang nhượng không thể tiếp tục đăng bán");
        }

        // 6. Kiểm tra đơn này chưa có bài đăng đang OPEN
        boolean alreadyListed = depositListingRepository
            .existsByOrderIdAndStatusIn(
                request.getOrderId(),
                List.of(DepositListingStatus.OPEN)
            );
        if (alreadyListed) {
            throw new RuntimeException("Đơn hàng này đã có bài đăng đang mở trên marketplace");
        }

        // 6. Lấy thông tin xe để lưu vào listing
        Vehicle vehicle = vehicleRepository.findById(order.getVehicleId())
            .orElseThrow(() -> new RuntimeException("Không tìm thấy xe"));

        // 7. Tính giá và Validate (Tối đa 60%)
        double originalDeposit = order.getDepositAmount();
        double sellingPrice    = request.getSellingPrice();
        
        if (sellingPrice > originalDeposit * SELLER_RATIO) {
            throw new RuntimeException("Giá bán không được vượt quá 60% tiền cọc gốc");
        }
        
        double platformFee     = originalDeposit - sellingPrice;

        // expiredAt = 00:00 ngày startDate - 24h
        LocalDateTime expiredAt = order.getStartDate().atStartOfDay().minusHours(24);

        // 8. Tạo DepositListing
        DepositListing listing = new DepositListing();
        listing.setSellerId(userId);
        listing.setOrderId(order.getId());
        listing.setVehicleId(vehicle.getId());
        listing.setOriginalDeposit(originalDeposit);
        listing.setSellingPrice(sellingPrice);
        listing.setPlatformFee(platformFee);
        listing.setExpiredAt(expiredAt);
        listing.setStatus(DepositListingStatus.OPEN);

        depositListingRepository.save(listing);

        // 9. Gửi thông báo cho A
        notificationService.create(
            userId,
            "Bài đăng suất cọc đã lên marketplace",
            "Suất cọc xe " + vehicle.getName()
                + " (ngày " + order.getStartDate() + ") đang chờ người mua.",
            NotificationType.DEPOSIT_LISTING,
            listing.getId()
        );

        // 10. Trả về response
        return toResponse(listing, vehicle, order);
    }

    // ----------------------------------------------------------------
    // CHỨC NĂNG 25: Xem danh sách suất cọc trên marketplace
    // ----------------------------------------------------------------
    public List<DepositListingResponse> getOpenListings() {

        // Chỉ lấy bài OPEN và expiredAt chưa qua
        List<DepositListing> listings = depositListingRepository
            .findByStatusAndExpiredAtAfter(
                DepositListingStatus.OPEN,
                LocalDateTime.now()
            );

        return listings.stream()
            .map(listing -> {
                Vehicle vehicle = vehicleRepository
                    .findById(listing.getVehicleId())
                    .orElse(null);

                Order order = orderRepository
                    .findById(listing.getOrderId())
                    .orElse(null);

                if (vehicle == null || order == null) return null;

                return toResponse(listing, vehicle, order);
            })
            .filter(r -> r != null)
            .collect(Collectors.toList());
    }

    // ----------------------------------------------------------------
    // CHỨC NĂNG 24: Xoá bài đăng suất cọc (Hủy bài)
    // ----------------------------------------------------------------
    public void cancelListing(String userId, String listingId) {

        // 1. Tìm bài đăng, kiểm tra tồn tại
        DepositListing listing = depositListingRepository.findById(listingId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy bài đăng suất cọc"));

        // 2. Chỉ người đăng mới được xoá
        if (!listing.getSellerId().equals(userId)) {
            throw new RuntimeException("Bạn không có quyền xoá bài đăng này");
        }

        // 3. Chỉ xoá được khi bài đang OPEN
        if (listing.getStatus() != DepositListingStatus.OPEN) {
            throw new RuntimeException(
                "Chỉ có thể xoá bài đăng đang ở trạng thái OPEN");
        }

        // 4. Đổi status → CANCELLED
        listing.setStatus(DepositListingStatus.CANCELLED);
        depositListingRepository.save(listing);

        // 5. Gửi thông báo cho A
        Vehicle vehicle = vehicleRepository.findById(listing.getVehicleId())
            .orElse(null);
        String vehicleName = vehicle != null ? vehicle.getName() : "xe";

        notificationService.create(
            userId,
            "Bài đăng suất cọc đã bị xoá",
            "Bạn đã xoá bài đăng suất cọc xe " + vehicleName
                + ". Lưu ý: tiền cọc sẽ mất nếu bạn không đến nhận xe.",
            NotificationType.DEPOSIT_LISTING,
            listingId
        );
    }

    // ----------------------------------------------------------------
    // CHỨC NƠNG 26: Mua suất cọc
    // ----------------------------------------------------------------
    public BuyDepositListingResponse buyListing(String buyerId,
                                                 BuyDepositListingRequest request)
            throws Exception {

        // 1. Tìm bài đăng
        DepositListing listing = depositListingRepository
            .findById(request.getListingId())
            .orElseThrow(() -> new RuntimeException("Không tìm thấy bài đăng suất cọc"));

        // 2. Bài phải đang OPEN
        if (listing.getStatus() != DepositListingStatus.OPEN) {
            throw new RuntimeException("Bài đăng này không còn khả dụng");
        }

        // 3. Chưa hết hạn
        if (LocalDateTime.now().isAfter(listing.getExpiredAt())) {
            throw new RuntimeException("Bài đăng này đã hết hạn");
        }

        // 4. B không được là A
        if (listing.getSellerId().equals(buyerId)) {
            throw new RuntimeException("Bạn không thể mua suất cọc của chính mình");
        }

        // 5. Kiểm tra B đã xác minh CCCD chưa
        User buyer = userRepository.findById(buyerId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        if (buyer.getIdentity() == null
                || buyer.getIdentity().getVerifyStatus()
                   != com.j2ee.carbooking.enums.VerifyStatus.VERIFIED) {
            throw new RuntimeException(
                "Bạn cần xác minh CCCD/GPLX trước khi mua suất cọc");
        }

        // 6. Lấy thông tin cần thiết
        Order order = orderRepository.findById(listing.getOrderId())
            .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        Vehicle vehicle = vehicleRepository.findById(listing.getVehicleId())
            .orElseThrow(() -> new RuntimeException("Không tìm thấy xe"));

        User seller = userRepository.findById(listing.getSellerId())
            .orElseThrow(() -> new RuntimeException("Không tìm thấy người bán"));

        double sellingPrice = listing.getSellingPrice();

        // 7. Xử lý thanh toán
        if (request.getPaymentMethod() == PaymentMethod.WALLET) {
            // --- Thanh toán bằng ví ---
            if (buyer.getWalletBalance() < sellingPrice) {
                throw new RuntimeException(
                    "Số dư ví không đủ. Cần "
                    + String.format("%,.0f", sellingPrice) + "đ, "
                    + "hiện có " + String.format("%,.0f", buyer.getWalletBalance()) + "đ");
            }

            // Trừ tiền ví B
            double buyerBalanceBefore = buyer.getWalletBalance();
            buyer.setWalletBalance(buyerBalanceBefore - sellingPrice);
            userRepository.save(buyer);

            // Tạo WalletTransaction cho B (PAY)
            WalletTransaction txBuy = new WalletTransaction();
            txBuy.setUserId(buyerId);
            txBuy.setType(TransactionType.PAY);
            txBuy.setAmount(sellingPrice);
            txBuy.setBalanceBefore(buyerBalanceBefore);
            txBuy.setBalanceAfter(buyer.getWalletBalance());
            txBuy.setRefType("DEPOSIT_LISTING");
            txBuy.setRefId(listing.getId());
            txBuy.setDescription("Mua suất cọc xe " + vehicle.getName());
            txBuy.setStatus(TransactionStatus.SUCCESS);
            walletTransactionRepository.save(txBuy);

            // Cộng tiền ví A (sellingPrice — phần A nhận được)
            double sellerBalanceBefore = seller.getWalletBalance();
            seller.setWalletBalance(sellerBalanceBefore + sellingPrice);
            userRepository.save(seller);

            // Tạo WalletTransaction cho A (RECEIVE)
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
            completeListing(listing, order, buyer, seller, vehicle);

            // Build response
            BuyDepositListingResponse res = new BuyDepositListingResponse();
            res.setListingId(listing.getId());
            res.setOrderId(order.getId());
            res.setVehicleName(vehicle.getName());
            res.setPaidAmount(sellingPrice);
            res.setPayUrl(null);
            res.setMessage("Mua suất cọc thành công! Đơn hàng đã được chuyển sang tên bạn.");
            return res;

        } else if (request.getPaymentMethod() == PaymentMethod.MOMO) {
            // --- Thanh toán bằng Momo ---
            // Lưu thông tin pending để xử lý sau khi callback
            // orderId Momo = "DEPOSIT-{listingId6}-{random}"
            String momoOrderId = "DEPOSIT-"
                + listing.getId().substring(0, Math.min(6, listing.getId().length()))
                + "-" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();

            // Tạo WalletTransaction PENDING cho B
            WalletTransaction txPending = new WalletTransaction();
            txPending.setUserId(buyerId);
            txPending.setType(TransactionType.PAY);
            txPending.setAmount(sellingPrice);
            txPending.setBalanceBefore(buyer.getWalletBalance());
            txPending.setBalanceAfter(buyer.getWalletBalance());
            txPending.setRefType("DEPOSIT_LISTING");
            txPending.setRefId(momoOrderId);
            txPending.setDescription("Mua suất cọc xe " + vehicle.getName()
                + "|LISTING:" + listing.getId()); // encode listingId vào description để dùng lúc callback
            txPending.setStatus(TransactionStatus.PENDING);
            walletTransactionRepository.save(txPending);

            String orderInfo = "Mua suat coc xe " + vehicle.getName();
            String payUrl = momoService.createPaymentUrl(
                momoOrderId, (long) sellingPrice, orderInfo);

            BuyDepositListingResponse res = new BuyDepositListingResponse();
            res.setListingId(listing.getId());
            res.setOrderId(order.getId());
            res.setVehicleName(vehicle.getName());
            res.setPaidAmount(sellingPrice);
            res.setPayUrl(payUrl);
            res.setMessage("Vui lòng hoàn tất thanh toán Momo để xác nhận mua suất cọc.");
            return res;

        } else {
            throw new RuntimeException("Phương thức thanh toán không hỗ trợ cho suất cọc");
        }
    }

    // ----------------------------------------------------------------
    // Helper: hoàn tất sang nhượng sau khi thanh toán xong
    // ----------------------------------------------------------------
    public void completeListing(DepositListing listing, Order order,
                                  User buyer, User seller, Vehicle vehicle) {
        // 1. Đổi listing → SOLD
        listing.setStatus(DepositListingStatus.SOLD);
        listing.setBuyerId(buyer.getId());
        listing.setSoldAt(LocalDateTime.now());
        depositListingRepository.save(listing);

        // 2. Sang nhượng đơn hàng: A → B
        order.setOriginalUserId(seller.getId()); // lưu A gốc
        order.setUserId(buyer.getId());          // đơn thuộc về B
        order.setIsTransferred(true);
        orderRepository.save(order);

        // 3. Thông báo cho B (người mua)
        notificationService.create(
            buyer.getId(),
            "Mua suất cọc thành công",
            "Bạn đã mua suất cọc xe " + vehicle.getName()
                + ". Nhớ đến nhận xe vào ngày " + order.getStartDate() + ".",
            NotificationType.DEPOSIT_LISTING,
            listing.getId()
        );

        // 4. Thông báo cho A (người bán)
        notificationService.create(
            seller.getId(),
            "Suất cọc đã được bán",
            "Suất cọc xe " + vehicle.getName()
                + " đã được bán thành công. Tiền đã vào ví của bạn.",
            NotificationType.DEPOSIT_LISTING,
            listing.getId()
        );
    }

    private DepositListingResponse toResponse(DepositListing listing,
                                               Vehicle vehicle,
                                               Order order) {
        DepositListingResponse res = new DepositListingResponse();

        res.setId(listing.getId());

        // Thông tin xe
        res.setVehicleId(vehicle.getId());
        res.setVehicleName(vehicle.getName());
        res.setVehicleBrand(vehicle.getBrand());
        res.setVehicleImage(
            vehicle.getImages() != null && !vehicle.getImages().isEmpty()
                ? vehicle.getImages().get(0)
                : null
        );

        // Thông tin đơn
        res.setOrderId(order.getId());
        res.setStartDate(order.getStartDate());
        res.setEndDate(order.getEndDate());
        res.setTotalDays(order.getTotalDays());

        // Tài chính
        res.setOriginalDeposit(listing.getOriginalDeposit());
        res.setSellingPrice(listing.getSellingPrice());
        res.setSavedAmount(listing.getOriginalDeposit() - listing.getSellingPrice());

        // Trạng thái
        res.setStatus(listing.getStatus());
        res.setExpiredAt(listing.getExpiredAt());
        res.setCreatedAt(listing.getCreatedAt());

        return res;
    }
}
