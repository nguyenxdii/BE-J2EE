package com.j2ee.carbooking.controller;

import com.j2ee.carbooking.model.Vehicle;
import com.j2ee.carbooking.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleRepository vehicleRepository;

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
}
