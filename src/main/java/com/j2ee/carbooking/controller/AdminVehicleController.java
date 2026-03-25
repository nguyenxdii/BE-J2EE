package com.j2ee.carbooking.controller;

import com.j2ee.carbooking.dto.response.AppApiResponse;
import com.j2ee.carbooking.enums.VehicleStatus;
import com.j2ee.carbooking.model.Specs;
import com.j2ee.carbooking.model.Vehicle;
import com.j2ee.carbooking.enums.OrderStatus;
import com.j2ee.carbooking.repository.VehicleRepository;
import com.j2ee.carbooking.repository.OrderRepository;
import com.j2ee.carbooking.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/admin/vehicles")
@RequiredArgsConstructor
public class AdminVehicleController {

    private final VehicleRepository vehicleRepository;
    private final OrderRepository orderRepository;
    private final CloudinaryService cloudinaryService;
    private final MongoTemplate mongoTemplate;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<AppApiResponse<Page<Vehicle>>> getVehicles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) VehicleStatus status) {
        
        List<Criteria> criteria = new ArrayList<>();
        if (categoryId != null && !categoryId.isBlank()) criteria.add(Criteria.where("categoryId").is(categoryId));
        if (brand != null && !brand.isBlank()) criteria.add(Criteria.where("brand").is(brand));
        if (status != null) criteria.add(Criteria.where("status").is(status));

        Query query = new Query();
        if (!criteria.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteria.toArray(new Criteria[0])));
        }
        
        long total = mongoTemplate.count(query, Vehicle.class);
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size), Sort.by(Sort.Direction.DESC, "createdAt"));
        query.with(pageable);
        
        List<Vehicle> data = mongoTemplate.find(query, Vehicle.class);
        Page<Vehicle> vehiclePage = new PageImpl<>(data, pageable, total);
        
        return ResponseEntity.ok(AppApiResponse.success("Lấy danh sách xe thành công", vehiclePage));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<AppApiResponse<Vehicle>> create(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String model,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String licensePlate,
            @RequestParam(required = false) Double pricePerDay,
            @RequestParam(required = false) Double depositAmount,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String fuelType,
            @RequestParam(required = false) String transmission,
            @RequestParam(required = false) Integer mileage,
            @RequestParam(required = false) String location,
            @RequestPart(required = false) List<MultipartFile> images) {
        
        if (name == null || licensePlate == null) {
            throw new RuntimeException("Tên xe và biển số là bắt buộc");
        }

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
        vehicle.setMileage(mileage != null ? mileage : 0);
        vehicle.setLocation(location != null ? location : "Hà Nội");
        
        Specs specs = new Specs();
        specs.setFuelType(fuelType);
        specs.setTransmission(transmission);
        vehicle.setSpecs(specs);
        
        if (images != null && !images.isEmpty()) {
            List<String> imageUrls = new ArrayList<>();
            for (MultipartFile file : images) {
                if (file != null && !file.isEmpty()) {
                    imageUrls.add(cloudinaryService.uploadImage(file, "vehicles"));
                }
            }
            vehicle.setImages(imageUrls);
        }
        
        return ResponseEntity.ok(AppApiResponse.success("Thêm xe thành công", vehicleRepository.save(vehicle)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<AppApiResponse<Vehicle>> update(
            @PathVariable String id,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String model,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Double pricePerDay,
            @RequestParam(required = false) Double depositAmount,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String fuelType,
            @RequestParam(required = false) String transmission,
            @RequestParam(required = false) Integer mileage,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) List<String> removedImages,
            @RequestPart(required = false) List<MultipartFile> newImages) {
        
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
        if (mileage != null) vehicle.setMileage(mileage);
        if (location != null) vehicle.setLocation(location);
        
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
        
        if (newImages != null) {
            for (MultipartFile file : newImages) {
                if (file != null && !file.isEmpty()) {
                    current.add(cloudinaryService.uploadImage(file, "vehicles"));
                }
            }
        }
        vehicle.setImages(current);

        return ResponseEntity.ok(AppApiResponse.success("Cập nhật xe thành công", vehicleRepository.save(vehicle)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/toggle-visibility")
    public ResponseEntity<AppApiResponse<Vehicle>> toggleVisibility(@PathVariable String id) {
        Vehicle vehicle = vehicleRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy xe"));
        vehicle.setStatus(vehicle.getStatus() == VehicleStatus.HIDDEN ? VehicleStatus.AVAILABLE : VehicleStatus.HIDDEN);
        return ResponseEntity.ok(AppApiResponse.success(vehicle.getStatus() == VehicleStatus.HIDDEN ? "Đã ẩn xe" : "Đã hiện xe", vehicleRepository.save(vehicle)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<AppApiResponse<Void>> delete(@PathVariable String id) {
        Vehicle vehicle = vehicleRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy xe"));
        
        boolean hasActiveOrders = orderRepository.existsByVehicleIdAndStatusIn(
                id, List.of(OrderStatus.PENDING, OrderStatus.CONFIRMED, OrderStatus.RENTING)
        );
        
        if (hasActiveOrders) {
            throw new RuntimeException("Không thể xoá xe đang có đơn hàng. Hãy ẩn xe thay vì xoá.");
        }

        if (vehicle.getImages() != null) {
            vehicle.getImages().forEach(cloudinaryService::deleteByUrl);
        }
        
        vehicleRepository.delete(vehicle);
        return ResponseEntity.ok(AppApiResponse.success("Đã xoá xe thành công"));
    }
}
