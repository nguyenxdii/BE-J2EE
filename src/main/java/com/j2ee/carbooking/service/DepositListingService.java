package com.j2ee.carbooking.service;

import com.j2ee.carbooking.dto.request.*;
import com.j2ee.carbooking.dto.response.*;
import com.j2ee.carbooking.enums.*;
import com.j2ee.carbooking.model.*;
import com.j2ee.carbooking.repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DepositListingService {

    private final DepositListingRepository depositListingRepository;
    private final OrderRepository orderRepository;
    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final MomoService momoService;
    private final NotificationService notificationService;

    public DepositListingService(DepositListingRepository depositListingRepository,
                                  OrderRepository orderRepository,
                                  VehicleRepository vehicleRepository,
                                  UserRepository userRepository,
                                  WalletTransactionRepository walletTransactionRepository,
                                  MomoService momoService,
                                  NotificationService notificationService) {
        this.depositListingRepository = depositListingRepository;
        this.orderRepository = orderRepository;
        this.vehicleRepository = vehicleRepository;
        this.userRepository = userRepository;
        this.walletTransactionRepository = walletTransactionRepository;
        this.momoService = momoService;
        this.notificationService = notificationService;
    }

    private static final double SELLER_RATIO = 0.6;

    public DepositListingResponse createListing(String userId, CreateDepositListingRequest request) {
        Order order = orderRepository.findById(request.getOrderId())
            .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("Bạn không có quyền đăng bán đơn hàng này");
        }

        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.CONFIRMED) {
            throw new RuntimeException("Chỉ có thể đăng bán suất cọc khi đơn đang PENDING hoặc CONFIRMED");
        }

        LocalDateTime deadline = order.getStartDate().atStartOfDay().minusHours(24);
        if (LocalDateTime.now().isAfter(deadline)) {
            throw new RuntimeException("Đã quá thời hạn đăng bán — cần đăng trước khi nhận xe ít nhất 24 tiếng");
        }

        boolean alreadyListed = depositListingRepository.existsByOrderIdAndStatusIn(request.getOrderId(), List.of(DepositListingStatus.OPEN));
        if (alreadyListed) {
            throw new RuntimeException("Đơn hàng này đã có bài đăng đang mở trên marketplace");
        }

        Vehicle vehicle = vehicleRepository.findById(order.getVehicleId())
            .orElseThrow(() -> new RuntimeException("Không tìm thấy xe"));

        double originalDeposit = order.getDepositAmount();
        double sellingPrice = originalDeposit * SELLER_RATIO;
        double platformFee = originalDeposit - sellingPrice;

        LocalDateTime expiredAt = order.getStartDate().atStartOfDay().minusHours(24);

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

        notificationService.create(userId, "Bài đăng suất cọc đã lên marketplace", "Suất cọc xe " + vehicle.getName() + " (ngày " + order.getStartDate() + ") đang chờ người mua.", NotificationType.DEPOSIT_LISTING, listing.getId());

        return toResponse(listing, vehicle, order);
    }

    public List<DepositListingResponse> getOpenListings() {
        List<DepositListing> listings = depositListingRepository.findByStatusAndExpiredAtAfter(DepositListingStatus.OPEN, LocalDateTime.now());
        return listings.stream()
            .map(listing -> {
                Vehicle vehicle = vehicleRepository.findById(listing.getVehicleId()).orElse(null);
                Order order = orderRepository.findById(listing.getOrderId()).orElse(null);
                if (vehicle == null || order == null) return null;
                return toResponse(listing, vehicle, order);
            })
            .filter(r -> r != null)
            .collect(Collectors.toList());
    }

    public void cancelListing(String userId, String listingId) {
        DepositListing listing = depositListingRepository.findById(listingId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy bài đăng suất cọc"));

        if (!listing.getSellerId().equals(userId)) {
            throw new RuntimeException("Bạn không có quyền xoá bài đăng này");
        }

        if (listing.getStatus() != DepositListingStatus.OPEN) {
            throw new RuntimeException("Chỉ có thể xoá bài đăng đang ở trạng thái OPEN");
        }

        listing.setStatus(DepositListingStatus.CANCELLED);
        depositListingRepository.save(listing);

        Vehicle vehicle = vehicleRepository.findById(listing.getVehicleId()).orElse(null);
        String vehicleName = vehicle != null ? vehicle.getName() : "xe";
        notificationService.create(userId, "Bài đăng suất cọc đã bị xoá", "Bạn đã xoá bài đăng suất cọc xe " + vehicleName + ". Lưu ý: tiền cọc sẽ mất nếu bạn không đến nhận xe.", NotificationType.DEPOSIT_LISTING, listingId);
    }

    public BuyDepositListingResponse buyListing(String buyerId, BuyDepositListingRequest request) throws Exception {
        DepositListing listing = depositListingRepository.findById(request.getListingId())
            .orElseThrow(() -> new RuntimeException("Không tìm thấy bài đăng suất cọc"));

        if (listing.getStatus() != DepositListingStatus.OPEN) {
            throw new RuntimeException("Bài đăng này không còn khả dụng");
        }

        if (LocalDateTime.now().isAfter(listing.getExpiredAt())) {
            throw new RuntimeException("Bài đăng này đã hết hạn");
        }

        if (listing.getSellerId().equals(buyerId)) {
            throw new RuntimeException("Bạn không thể mua suất cọc của chính mình");
        }

        User buyer = userRepository.findById(buyerId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        if (buyer.getIdentity() == null || buyer.getIdentity().getVerifyStatus() != com.j2ee.carbooking.enums.VerifyStatus.VERIFIED) {
            throw new RuntimeException("Bạn cần xác minh CCCD/GPLX trước khi mua suất cọc");
        }

        Order order = orderRepository.findById(listing.getOrderId()).orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));
        Vehicle vehicle = vehicleRepository.findById(listing.getVehicleId()).orElseThrow(() -> new RuntimeException("Không tìm thấy xe"));
        User seller = userRepository.findById(listing.getSellerId()).orElseThrow(() -> new RuntimeException("Không tìm thấy người bán"));
        double sellingPrice = listing.getSellingPrice();

        if (request.getPaymentMethod() == PaymentMethod.WALLET) {
            if (buyer.getWalletBalance() < sellingPrice) {
                throw new RuntimeException("Số dư ví không đủ. Cần " + String.format("%,.0f", sellingPrice) + "đ, hiện có " + String.format("%,.0f", buyer.getWalletBalance()) + "đ");
            }

            double buyerBalanceBefore = buyer.getWalletBalance();
            buyer.setWalletBalance(buyerBalanceBefore - sellingPrice);
            userRepository.save(buyer);

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

            completeListing(listing, order, buyer, seller, vehicle);

            BuyDepositListingResponse res = new BuyDepositListingResponse();
            res.setListingId(listing.getId());
            res.setOrderId(order.getId());
            res.setVehicleName(vehicle.getName());
            res.setPaidAmount(sellingPrice);
            res.setPayUrl(null);
            res.setMessage("Mua suất cọc thành công! Đơn hàng đã được chuyển sang tên bạn.");
            return res;

        } else if (request.getPaymentMethod() == PaymentMethod.MOMO) {
            String momoOrderId = "DEPOSIT-" + listing.getId().substring(0, Math.min(6, listing.getId().length())) + "-" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();

            WalletTransaction txPending = new WalletTransaction();
            txPending.setUserId(buyerId);
            txPending.setType(TransactionType.PAY);
            txPending.setAmount(sellingPrice);
            txPending.setBalanceBefore(buyer.getWalletBalance());
            txPending.setBalanceAfter(buyer.getWalletBalance());
            txPending.setRefType("DEPOSIT_LISTING");
            txPending.setRefId(momoOrderId);
            txPending.setDescription("Mua suất cọc xe " + vehicle.getName() + "|LISTING:" + listing.getId());
            txPending.setStatus(TransactionStatus.PENDING);
            walletTransactionRepository.save(txPending);

            String orderInfo = "Mua suat coc xe " + vehicle.getName();
            String payUrl = momoService.createPaymentUrl(momoOrderId, (long) sellingPrice, orderInfo);

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

    public void completeListing(DepositListing listing, Order order, User buyer, User seller, Vehicle vehicle) {
        listing.setStatus(DepositListingStatus.SOLD);
        listing.setBuyerId(buyer.getId());
        listing.setSoldAt(LocalDateTime.now());
        depositListingRepository.save(listing);

        order.setOriginalUserId(seller.getId());
        order.setUserId(buyer.getId());
        order.setIsTransferred(true);
        orderRepository.save(order);

        notificationService.create(buyer.getId(), "Mua suất cọc thành công", "Bạn đã mua suất cọc xe " + vehicle.getName() + ". Nhớ đến nhận xe vào ngày " + order.getStartDate() + ".", NotificationType.DEPOSIT_LISTING, listing.getId());
        notificationService.create(seller.getId(), "Suất cọc đã được bán", "Suất cọc xe " + vehicle.getName() + " đã được bán thành công. Tiền đã vào ví của bạn.", NotificationType.DEPOSIT_LISTING, listing.getId());
    }

    private DepositListingResponse toResponse(DepositListing listing, Vehicle vehicle, Order order) {
        DepositListingResponse res = new DepositListingResponse();
        res.setId(listing.getId());
        res.setVehicleId(vehicle.getId());
        res.setVehicleName(vehicle.getName());
        res.setVehicleBrand(vehicle.getBrand());
        res.setVehicleImage(vehicle.getImages() != null && !vehicle.getImages().isEmpty() ? vehicle.getImages().get(0) : null);
        res.setOrderId(order.getId());
        res.setStartDate(order.getStartDate());
        res.setEndDate(order.getEndDate());
        res.setTotalDays(order.getTotalDays());
        res.setOriginalDeposit(listing.getOriginalDeposit());
        res.setSellingPrice(listing.getSellingPrice());
        res.setSavedAmount(listing.getOriginalDeposit() - listing.getSellingPrice());
        res.setStatus(listing.getStatus());
        res.setExpiredAt(listing.getExpiredAt());
        res.setCreatedAt(listing.getCreatedAt());
        return res;
    }
}
