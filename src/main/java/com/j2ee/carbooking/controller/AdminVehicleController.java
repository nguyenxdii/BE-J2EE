package com.j2ee.carbooking.controller;

import com.j2ee.carbooking.dto.response.AppApiResponse;
import com.j2ee.carbooking.enums.VehicleStatus;
import com.j2ee.carbooking.model.Vehicle;
import com.j2ee.carbooking.enums.OrderStatus;
import com.j2ee.carbooking.repository.VehicleRepository;
import com.j2ee.carbooking.repository.OrderRepository;
import com.j2ee.carbooking.service.CloudinaryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/admin/vehicles")
public class AdminVehicleController {

    private final VehicleRepository vehicleRepository;
    private final OrderRepository orderRepository;
    private final CloudinaryService cloudinaryService;

    public AdminVehicleController(VehicleRepository vehicleRepository, OrderRepository orderRepository, CloudinaryService cloudinaryService) {
        this.vehicleRepository = vehicleRepository;
        this.orderRepository = orderRepository;
        this.cloudinaryService = cloudinaryService;
    }

    @GetMapping
    public ResponseEntity<AppApiResponse<Page<Vehicle>>> getVehicles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) VehicleStatus status) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        // Ở đây có thể dùng Criteria để lọc categoryId và status
        Page<Vehicle> vehicles = vehicleRepository.findAll(pageable);
        
        return ResponseEntity.ok(AppApiResponse.success("Lấy danh sách xe thành công", vehicles));
    }

    @PostMapping
    public ResponseEntity<AppApiResponse<Vehicle>> create(
            @ModelAttribute Vehicle vehicle,
            @RequestParam(value = "files", required = false) List<MultipartFile> files) {
        
        if (files != null && !files.isEmpty()) {
            List<String> imageUrls = new ArrayList<>();
            for (MultipartFile file : files) {
                imageUrls.add(cloudinaryService.upload(file));
            }
            vehicle.setImages(imageUrls);
        }
        
        vehicle.setStatus(VehicleStatus.AVAILABLE);
        return ResponseEntity.ok(AppApiResponse.success("Thêm xe thành công", vehicleRepository.save(vehicle)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AppApiResponse<Vehicle>> update(
            @PathVariable String id,
            @ModelAttribute Vehicle vehicleUpdate,
            @RequestParam(value = "files", required = false) List<MultipartFile> files,
            @RequestParam(value = "removeImages", required = false) List<String> removeImages) {
        
        Vehicle vehicle = vehicleRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy xe"));
        
        // Cập nhật thông tin cơ bản
        vehicle.setName(vehicleUpdate.getName());
        vehicle.setBrand(vehicleUpdate.getBrand());
        vehicle.setModel(vehicleUpdate.getModel());
        vehicle.setYear(vehicleUpdate.getYear());
        vehicle.setLicensePlate(vehicleUpdate.getLicensePlate());
        vehicle.setCategoryId(vehicleUpdate.getCategoryId());
        vehicle.setPricePerDay(vehicleUpdate.getPricePerDay());
        if (vehicleUpdate.getSpecs() != null) {
            if (vehicle.getSpecs() == null) vehicle.setSpecs(new com.j2ee.carbooking.model.Specs());
            vehicle.getSpecs().setTransmission(vehicleUpdate.getSpecs().getTransmission());
            vehicle.getSpecs().setFuelType(vehicleUpdate.getSpecs().getFuelType());
            vehicle.getSpecs().setSeats(vehicleUpdate.getSpecs().getSeats());
        }
        vehicle.setDescription(vehicleUpdate.getDescription());

        // Xóa ảnh cũ
        if (removeImages != null && !removeImages.isEmpty()) {
            List<String> currentImages = vehicle.getImages();
            for (String img : removeImages) {
                if (currentImages.remove(img)) {
                    cloudinaryService.delete(img);
                }
            }
            vehicle.setImages(currentImages);
        }

        // Thêm ảnh mới
        if (files != null && !files.isEmpty()) {
            List<String> currentImages = vehicle.getImages();
            if (currentImages == null) currentImages = new ArrayList<>();
            for (MultipartFile file : files) {
                currentImages.add(cloudinaryService.upload(file));
            }
            vehicle.setImages(currentImages);
        }

        return ResponseEntity.ok(AppApiResponse.success("Cập nhật xe thành công", vehicleRepository.save(vehicle)));
    }

    @PutMapping("/{id}/hide")
    public ResponseEntity<AppApiResponse<Vehicle>> hide(@PathVariable String id) {
        Vehicle vehicle = vehicleRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy xe"));
        vehicle.setStatus(VehicleStatus.HIDDEN);
        return ResponseEntity.ok(AppApiResponse.success("Đã ẩn xe", vehicleRepository.save(vehicle)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<AppApiResponse<Void>> delete(@PathVariable String id) {
        Vehicle vehicle = vehicleRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy xe"));
        
        // Kiểm tra đơn hàng active (CONFIRMED hoặc RENTING)
        boolean hasActiveOrders = orderRepository.findAll().stream()
            .anyMatch(o -> o.getVehicleId().equals(id) && 
                (o.getStatus() == OrderStatus.CONFIRMED || o.getStatus() == OrderStatus.RENTING));
        
        if (hasActiveOrders) {
            throw new RuntimeException("Không thể xoá xe đang có đơn hàng chờ nhận hoặc đang thuê.");
        }

        // Xóa toàn bộ ảnh Cloudinary
        if (vehicle.getImages() != null) {
            for (String img : vehicle.getImages()) {
                cloudinaryService.delete(img);
            }
        }
        
        vehicleRepository.delete(vehicle);
        return ResponseEntity.ok(AppApiResponse.success("Đã xoá xe thành công"));
    }
}
