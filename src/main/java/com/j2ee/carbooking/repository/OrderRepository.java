package com.j2ee.carbooking.repository;

import com.j2ee.carbooking.enums.OrderStatus;
import com.j2ee.carbooking.model.Order;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends MongoRepository<Order, String> {
    List<Order> findByUserId(String userId);
    List<Order> findByVehicleId(String vehicleId);
    Optional<Order> findByOrderCode(String orderCode);
    Optional<Order> findByIdAndUserId(String id, String userId);

    // Kiểm tra xe có đơn active trong khoảng ngày không
    // Dùng để validate khi đặt xe mới
    boolean existsByVehicleIdAndStatusInAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
        String vehicleId,
        List<OrderStatus> statuses,
        LocalDate endDate,
        LocalDate startDate
    );

    // Lấy danh sách order đang chiếm lịch của các xe (phục vụ tìm kiếm xe trống theo ngày)
    List<Order> findByVehicleIdInAndStatusInAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
        List<String> vehicleIds,
        List<OrderStatus> statuses,
        LocalDate endDate,
        LocalDate startDate
    );

    List<Order> findByVehicleIdAndStatusInAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
        String vehicleId,
        List<OrderStatus> statuses,
        LocalDate endDate,
        LocalDate startDate
    );

    boolean existsByVehicleIdAndStatusIn(String vehicleId, List<OrderStatus> statuses);

    // Lấy đơn của user theo trạng thái
    List<Order> findByUserIdAndStatus(String userId, OrderStatus status);

    // Thống kê — đếm đơn theo trạng thái
    long countByStatus(OrderStatus status);
}
