package com.j2ee.carbooking.service;

import com.j2ee.carbooking.enums.DepositListingStatus;
import com.j2ee.carbooking.enums.OrderStatus;
import com.j2ee.carbooking.model.DepositListing;
import com.j2ee.carbooking.model.Order;
import com.j2ee.carbooking.model.Vehicle;
import com.j2ee.carbooking.repository.DepositListingRepository;
import com.j2ee.carbooking.repository.OrderRepository;
import com.j2ee.carbooking.repository.VehicleRepository;
import com.j2ee.carbooking.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StatisticsService {
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final VehicleRepository vehicleRepository;
    private final DepositListingRepository depositListingRepository;

    public StatisticsService(UserRepository userRepository, OrderRepository orderRepository, VehicleRepository vehicleRepository, DepositListingRepository depositListingRepository) {
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.vehicleRepository = vehicleRepository;
        this.depositListingRepository = depositListingRepository;
    }

    // Lấy doanh thu theo ngày trong tháng hoặc khoảng thời gian
    public Map<String, Object> getRevenueStats(Integer month, Integer year) {
        List<Order> orders = orderRepository.findAll();
        List<DepositListing> listings = depositListingRepository.findAll();

        // Nếu không có month/year, lấy 30 ngày gần nhất
        boolean isFullMonth = (month != null && year != null);
        
        // Lọc theo tháng/năm
        List<Order> filteredOrders = orders.stream()
            .filter(o -> o.getStatus() == OrderStatus.COMPLETED)
            .filter(o -> {
                if (isFullMonth) {
                    return o.getCreatedAt().getMonthValue() == month && o.getCreatedAt().getYear() == year;
                } else {
                    return o.getCreatedAt().isAfter(java.time.LocalDateTime.now().minusDays(30));
                }
            })
            .collect(Collectors.toList());

        List<DepositListing> filteredListings = listings.stream()
            .filter(l -> l.getStatus() == DepositListingStatus.SOLD)
            .filter(l -> l.getSoldAt() != null)
            .filter(l -> {
                if (isFullMonth) {
                    return l.getSoldAt().getMonthValue() == month && l.getSoldAt().getYear() == year;
                } else {
                    return l.getSoldAt().isAfter(java.time.LocalDateTime.now().minusDays(30));
                }
            })
            .collect(Collectors.toList());

        Map<String, Double> rentalRevenueByDay = new TreeMap<>();
        Map<String, Double> platformFeeByDay = new TreeMap<>();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // Gom doanh thu thuê xe
        for (Order o : filteredOrders) {
            String day = o.getCreatedAt().format(formatter);
            rentalRevenueByDay.put(day, rentalRevenueByDay.getOrDefault(day, 0.0) + o.getRentalPrice());
        }

        // Gom phí suất cọc
        for (DepositListing l : filteredListings) {
            String day = l.getSoldAt().format(formatter);
            platformFeeByDay.put(day, platformFeeByDay.getOrDefault(day, 0.0) + l.getPlatformFee());
        }

        return Map.of(
            "rentalRevenue", rentalRevenueByDay,
            "platformFee", platformFeeByDay,
            "totalRental", filteredOrders.stream().mapToDouble(Order::getRentalPrice).sum(),
            "totalPlatform", filteredListings.stream().mapToDouble(DepositListing::getPlatformFee).sum()
        );
    }

    public Map<String, Object> getOrderVehicleStats() {
        List<Order> allOrders = orderRepository.findAll();

        // 1. Top xe thuê nhiều nhất
        Map<String, Long> vehicleOrderCount = allOrders.stream()
            .collect(Collectors.groupingBy(Order::getVehicleId, Collectors.counting()));

        List<Map<String, Object>> topVehicles = vehicleOrderCount.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(5)
            .map(entry -> {
                Vehicle v = vehicleRepository.findById(entry.getKey()).orElse(null);
                Map<String, Object> map = new HashMap<>();
                map.put("id", entry.getKey());
                map.put("name", v != null ? v.getName() : "Unknown");
                map.put("count", entry.getValue());
                return map;
            })
            .collect(Collectors.toList());

        // 2. Tỉ lệ đơn
        long total = allOrders.size();
        long cancelled = allOrders.stream().filter(o -> o.getStatus() == OrderStatus.CANCELLED).count();
        double cancellationRate = total > 0 ? (double) cancelled / total * 100 : 0;

        // 3. Biểu đồ trạng thái
        Map<OrderStatus, Long> statusDistribution = allOrders.stream()
            .collect(Collectors.groupingBy(Order::getStatus, Collectors.counting()));

        return Map.of(
            "topVehicles", topVehicles,
            "cancellationRate", cancellationRate,
            "statusDistribution", statusDistribution,
            "totalOrders", total
        );
    }

    public Map<String, Object> getDashboardOverview() {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.time.LocalDateTime startOfMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        java.time.LocalDateTime startOfToday = now.withHour(0).withMinute(0).withSecond(0);

        List<Order> allOrders = orderRepository.findAll();
        List<DepositListing> allListings = depositListingRepository.findAll();

        // 1. Doanh thu tháng này (từ đơn COMPLETED)
        double monthlyRevenue = allOrders.stream()
            .filter(o -> o.getStatus() == OrderStatus.COMPLETED && o.getCreatedAt().isAfter(startOfMonth))
            .mapToDouble(Order::getRentalPrice).sum()
            + allListings.stream()
            .filter(l -> l.getStatus() == DepositListingStatus.SOLD && l.getSoldAt() != null && l.getSoldAt().isAfter(startOfMonth))
            .mapToDouble(DepositListing::getPlatformFee).sum();

        // 2. Số đơn mới hôm nay (PENDING)
        long ordersToday = allOrders.stream()
            .filter(o -> o.getStatus() == OrderStatus.PENDING && o.getCreatedAt().isAfter(startOfToday))
            .count();

        // 3. Số user mới trong tháng
        long newUsersMonth = userRepository.countByCreatedAtBetween(startOfMonth, now);

        // 4. Số xe đang RENTING
        long rentingVehicles = allOrders.stream()
            .filter(o -> o.getStatus() == OrderStatus.RENTING)
            .count();

        // 5. Yêu cầu chờ duyệt (CCCD PENDING + Đơn PENDING)
        long pendingCCCD = userRepository.countByIdentityVerifyStatus(com.j2ee.carbooking.enums.VerifyStatus.PENDING);
        long pendingOrders = allOrders.stream().filter(o -> o.getStatus() == OrderStatus.PENDING).count();

        // 6. Biểu đồ 7 ngày gần nhất
        Map<String, Double> revenue7Days = new LinkedHashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");
        for (int i = 6; i >= 0; i--) {
            java.time.LocalDate date = java.time.LocalDate.now().minusDays(i);
            String label = date.format(formatter);
            
            double dailyRev = allOrders.stream()
                .filter(o -> o.getStatus() == OrderStatus.COMPLETED && o.getCreatedAt().toLocalDate().equals(date))
                .mapToDouble(Order::getRentalPrice).sum()
                + allListings.stream()
                .filter(l -> l.getStatus() == DepositListingStatus.SOLD && l.getSoldAt() != null && l.getSoldAt().toLocalDate().equals(date))
                .mapToDouble(DepositListing::getPlatformFee).sum();
            
            revenue7Days.put(label, dailyRev);
        }

        return Map.of(
            "monthlyRevenue", monthlyRevenue,
            "ordersToday", ordersToday,
            "newUsersMonth", newUsersMonth,
            "rentingVehicles", rentingVehicles,
            "pendingRequests", pendingCCCD + pendingOrders,
            "revenue7Days", revenue7Days
        );
    }
}
