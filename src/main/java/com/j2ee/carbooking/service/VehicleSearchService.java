package com.j2ee.carbooking.service;

import com.j2ee.carbooking.enums.OrderStatus;
import com.j2ee.carbooking.enums.VehicleStatus;
import com.j2ee.carbooking.model.Order;
import com.j2ee.carbooking.model.Vehicle;
import com.j2ee.carbooking.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class VehicleSearchService {

    private static final int DEFAULT_LIMIT = 200;

    private final MongoTemplate mongoTemplate;
    private final OrderRepository orderRepository;

    public List<Vehicle> search(
            String keyword,
            String categoryId,
            String brand,
            Double minPrice,
            Double maxPrice,
            LocalDate rentalDate,
            String sort
    ) {
        List<Criteria> ands = new ArrayList<>();

        // Chỉ hiển thị xe AVAILABLE (Admin ẩn tạm thời = HIDDEN)
        ands.add(Criteria.where("status").is(VehicleStatus.AVAILABLE));

        keyword = keyword == null ? null : keyword.trim();
        if (keyword != null && !keyword.isBlank()) {
            String[] words = keyword.split("\\s+");
            List<Criteria> wordCriteria = new ArrayList<>();
            for (String word : words) {
                if (!word.isBlank()) {
                    String escaped = Pattern.quote(word);
                    wordCriteria.add(Criteria.where("name").regex(".*" + escaped + ".*", "i"));
                }
            }
            if (!wordCriteria.isEmpty()) {
                ands.add(new Criteria().andOperator(wordCriteria.toArray(new Criteria[0])));
            }
        }

        if (categoryId != null && !categoryId.isBlank()) {
            ands.add(Criteria.where("categoryId").is(categoryId));
        }

        if (brand != null && !brand.isBlank()) {
            ands.add(Criteria.where("brand").is(brand));
        }

        if (minPrice != null) {
            ands.add(Criteria.where("pricePerDay").gte(minPrice));
        }
        if (maxPrice != null) {
            ands.add(Criteria.where("pricePerDay").lte(maxPrice));
        }

        Criteria criteria = new Criteria().andOperator(ands.toArray(new Criteria[0]));
        Query query = new Query(criteria);

        query.with(resolveSort(sort));
        query.limit(DEFAULT_LIMIT);

        List<Vehicle> candidates = mongoTemplate.find(query, Vehicle.class);

        // Nếu có rentalDate thì loại bỏ những xe có order đang chiếm lịch trong ngày đó.
        if (rentalDate != null && !candidates.isEmpty()) {
            List<String> vehicleIds = candidates.stream().map(Vehicle::getId).toList();
            List<OrderStatus> activeStatuses = List.of(
                    OrderStatus.PENDING,
                    OrderStatus.CONFIRMED,
                    OrderStatus.RENTING
            );

            // Overlap với 1 ngày D: order.startDate <= D và order.endDate >= D
            List<Order> busyOrders = orderRepository
                    .findByVehicleIdInAndStatusInAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                            vehicleIds,
                            activeStatuses,
                            rentalDate,
                            rentalDate
                    );

            Set<String> unavailableVehicleIds = new HashSet<>();
            for (Order o : busyOrders) {
                if (o.getVehicleId() != null) {
                    unavailableVehicleIds.add(o.getVehicleId());
                }
            }

            candidates.removeIf(v -> unavailableVehicleIds.contains(v.getId()));
        }

        return candidates;
    }

    private Sort resolveSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return Sort.by(Sort.Direction.DESC, "avgRating");
        }
        return switch (sort) {
            case "priceAsc" -> Sort.by(Sort.Direction.ASC, "pricePerDay");
            case "priceDesc" -> Sort.by(Sort.Direction.DESC, "pricePerDay");
            case "ratingDesc", "topRating" -> Sort.by(Sort.Direction.DESC, "avgRating");
            default -> Sort.by(Sort.Direction.DESC, "avgRating");
        };
    }
}

