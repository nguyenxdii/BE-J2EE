package com.j2ee.carbooking.controller;

import com.j2ee.carbooking.enums.OrderStatus;
import com.j2ee.carbooking.enums.VehicleStatus;
import com.j2ee.carbooking.model.Specs;
import com.j2ee.carbooking.model.Vehicle;
import com.j2ee.carbooking.repository.OrderRepository;
import com.j2ee.carbooking.repository.VehicleRepository;
import com.j2ee.carbooking.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final VehicleRepository vehicleRepository;
    private final OrderRepository orderRepository;
    private final CloudinaryService cloudinaryService;
    private final MongoTemplate mongoTemplate;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/vehicles")
    public Map<String, Object> getVehicles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) VehicleStatus status
    ) {
        List<Criteria> criteria = new ArrayList<>();
        if (categoryId != null && !categoryId.isBlank()) criteria.add(Criteria.where("categoryId").is(categoryId));
        if (brand != null && !brand.isBlank()) criteria.add(Criteria.where("brand").is(brand));
        if (status != null) criteria.add(Criteria.where("status").is(status));

        Query query = new Query();
        if (!criteria.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteria.toArray(new Criteria[0])));
        }
        long total = mongoTemplate.count(query, Vehicle.class);
        query.with(PageRequest.of(Math.max(0, page), Math.max(1, size), Sort.by(Sort.Direction.DESC, "createdAt")));
        List<Vehicle> data = mongoTemplate.find(query, Vehicle.class);
        return Map.of("data", data, "total", total);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/vehicles", consumes = {"multipart/form-data"})
    public Vehicle createVehicle(
            @RequestParam String name,
            @RequestParam String categoryId,
            @RequestParam String brand,
            @RequestParam String model,
            @RequestParam Integer year,
            @RequestParam String licensePlate,
            @RequestParam Double pricePerDay,
            @RequestParam Double depositAmount,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String fuelType,
            @RequestParam(required = false) String transmission,
            @RequestPart(required = false) List<MultipartFile> images
    ) {
        if (vehicleRepository.existsByLicensePlate(licensePlate)) {
            throw new RuntimeException("Biển số đã tồn tại");
        }
        Vehicle vehicle = new Vehicle();
        vehicle.setName(name);
        vehicle.setCategoryId(categoryId);
        vehicle.setBrand(brand);
        vehicle.setModel(model);
        vehicle.setYear(year);
        vehicle.setLicensePlate(licensePlate);
        vehicle.setPricePerDay(pricePerDay);
        vehicle.setDepositAmount(depositAmount);
        vehicle.setDescription(description);
        vehicle.setStatus(VehicleStatus.AVAILABLE);
        Specs specs = new Specs();
        specs.setFuelType(fuelType);
        specs.setTransmission(transmission);
        vehicle.setSpecs(specs);
        vehicle.setImages(uploadImages(images));
        return vehicleRepository.save(vehicle);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(value = "/vehicles/{id}", consumes = {"multipart/form-data"})
    public Vehicle updateVehicle(
            @PathVariable String id,
            @RequestParam String name,
            @RequestParam String categoryId,
            @RequestParam String brand,
            @RequestParam String model,
            @RequestParam Integer year,
            @RequestParam Double pricePerDay,
            @RequestParam Double depositAmount,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String fuelType,
            @RequestParam(required = false) String transmission,
            @RequestParam(required = false) List<String> removedImages,
            @RequestPart(required = false) List<MultipartFile> newImages
    ) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy xe"));
        vehicle.setName(name);
        vehicle.setCategoryId(categoryId);
        vehicle.setBrand(brand);
        vehicle.setModel(model);
        vehicle.setYear(year);
        vehicle.setPricePerDay(pricePerDay);
        vehicle.setDepositAmount(depositAmount);
        vehicle.setDescription(description);
        Specs specs = vehicle.getSpecs() == null ? new Specs() : vehicle.getSpecs();
        specs.setFuelType(fuelType);
        specs.setTransmission(transmission);
        vehicle.setSpecs(specs);

        List<String> current = vehicle.getImages() == null ? new ArrayList<>() : new ArrayList<>(vehicle.getImages());
        if (removedImages != null) {
            for (String removed : removedImages) {
                current.remove(removed);
                cloudinaryService.deleteByUrl(removed);
            }
        }
        current.addAll(uploadImages(newImages));
        vehicle.setImages(current);

        return vehicleRepository.save(vehicle);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/vehicles/{id}/hide")
    public Vehicle hideVehicle(@PathVariable String id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy xe"));
        vehicle.setStatus(VehicleStatus.HIDDEN);
        return vehicleRepository.save(vehicle);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/vehicles/{id}")
    public Map<String, Object> deleteVehicle(@PathVariable String id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy xe"));
        boolean hasActiveOrders = orderRepository.existsByVehicleIdAndStatusIn(
                id, List.of(OrderStatus.PENDING, OrderStatus.CONFIRMED, OrderStatus.RENTING)
        );
        if (hasActiveOrders) {
            throw new RuntimeException("Không thể xóa xe vì đang có đơn active");
        }
        if (vehicle.getImages() != null) {
            vehicle.getImages().forEach(cloudinaryService::deleteByUrl);
        }
        vehicleRepository.deleteById(id);
        return Map.of("deleted", true);
    }

    private List<String> uploadImages(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return new ArrayList<>();
        }
        return files.stream()
                .filter(file -> file != null && !file.isEmpty())
                .map(file -> cloudinaryService.uploadImage(file, "vehicles"))
                .toList();
    }
}
