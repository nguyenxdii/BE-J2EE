package com.j2ee.carbooking.service;

import com.j2ee.carbooking.dto.request.CreateDepositListingRequest;
import com.j2ee.carbooking.dto.response.DepositListingResponse;
import com.j2ee.carbooking.enums.DepositListingStatus;
import com.j2ee.carbooking.enums.NotificationType;
import com.j2ee.carbooking.enums.OrderStatus;
import com.j2ee.carbooking.model.DepositListing;
import com.j2ee.carbooking.model.Order;
import com.j2ee.carbooking.model.Vehicle;
import com.j2ee.carbooking.repository.DepositListingRepository;
import com.j2ee.carbooking.repository.OrderRepository;
import com.j2ee.carbooking.repository.VehicleRepository;
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

        // 5. Kiểm tra đơn này chưa có bài đăng đang OPEN
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

        // 7. Tính giá
        double originalDeposit = order.getDepositAmount();
        double sellingPrice    = originalDeposit * SELLER_RATIO;
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
