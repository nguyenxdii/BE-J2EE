package com.j2ee.carbooking.controller;

import com.j2ee.carbooking.model.Vehicle;
import com.j2ee.carbooking.repository.VehicleRepository;
import com.j2ee.carbooking.service.VehicleSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import org.springframework.format.annotation.DateTimeFormat;

import java.util.List;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleRepository vehicleRepository;
    private final VehicleSearchService vehicleSearchService;

    @GetMapping
    public List<Vehicle> getAllVehicles() {
        return vehicleRepository.findAll();
    }

    @GetMapping("/{id}")
    public Vehicle getVehicleById(@PathVariable String id) {
        return vehicleRepository.findById(id).orElse(null);
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
}
