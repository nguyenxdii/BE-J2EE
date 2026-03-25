package com.j2ee.carbooking.controller;

import com.j2ee.carbooking.enums.OrderStatus;
import com.j2ee.carbooking.enums.VehicleStatus;
import com.j2ee.carbooking.model.Order;
import com.j2ee.carbooking.model.Vehicle;
import com.j2ee.carbooking.repository.OrderRepository;
import com.j2ee.carbooking.repository.VehicleRepository;
import com.j2ee.carbooking.service.VehicleSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.YearMonth;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleRepository vehicleRepository;
    private final VehicleSearchService vehicleSearchService;
    private final OrderRepository orderRepository;

    @GetMapping
    public List<Vehicle> getAllVehicles() {
        return vehicleRepository.findByStatus(VehicleStatus.AVAILABLE);
    }

    @GetMapping("/{id}")
    public Vehicle getVehicleById(@PathVariable String id) {
        return vehicleRepository.findById(id).orElse(null);
    }

    @GetMapping("/featured")
    public List<Vehicle> getFeaturedVehicles() {
        return vehicleRepository.findTop8ByStatusOrderByAvgRatingDesc(VehicleStatus.AVAILABLE);
    }

    @GetMapping("/category/{categoryId}")
    public List<Vehicle> getVehiclesByCategory(@PathVariable String categoryId) {
        return vehicleRepository.findByCategoryId(categoryId);
    }

    // GET /api/vehicles/search?keyword=...&categoryId=...&brand=...&minPrice=...&maxPrice=...&rentalDate=yyyy-MM-dd&sort=priceAsc|priceDesc|ratingDesc
    @GetMapping("/search")
    public List<Vehicle> searchVehicles(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate rentalDate,
            @RequestParam(required = false, defaultValue = "ratingDesc") String sort
    ) {
        return vehicleSearchService.search(
                keyword,
                categoryId,
                brand,
                minPrice,
                maxPrice,
                rentalDate,
                sort
        );
    }

    @GetMapping("/{id}/availability")
    public Map<String, Object> getMonthlyAvailability(
            @PathVariable String id,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth month
    ) {
        LocalDate firstDay = month.atDay(1);
        LocalDate lastDay = month.atEndOfMonth();
        List<OrderStatus> activeStatuses = List.of(OrderStatus.PENDING, OrderStatus.CONFIRMED, OrderStatus.RENTING);
        List<Order> occupiedOrders = orderRepository
                .findByVehicleIdAndStatusInAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                        id, activeStatuses, lastDay, firstDay
                );

        Set<LocalDate> occupiedDates = new HashSet<>();
        for (Order order : occupiedOrders) {
            LocalDate current = order.getStartDate().isBefore(firstDay) ? firstDay : order.getStartDate();
            LocalDate max = order.getEndDate().isAfter(lastDay) ? lastDay : order.getEndDate();
            while (!current.isAfter(max)) {
                occupiedDates.add(current);
                current = current.plusDays(1);
            }
        }

        return Map.of(
                "vehicleId", id,
                "month", month.toString(),
                "occupiedDates", occupiedDates.stream().map(LocalDate::toString).sorted().toList()
        );
    }
}
