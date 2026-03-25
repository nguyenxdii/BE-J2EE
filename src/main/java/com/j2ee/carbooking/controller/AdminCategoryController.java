package com.j2ee.carbooking.controller;

import com.j2ee.carbooking.dto.response.AppApiResponse;
import com.j2ee.carbooking.model.Category;
import com.j2ee.carbooking.repository.CategoryRepository;
import com.j2ee.carbooking.repository.VehicleRepository;
import com.j2ee.carbooking.service.CloudinaryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/categories")
public class AdminCategoryController {

    private final CategoryRepository categoryRepository;
    private final VehicleRepository vehicleRepository;
    private final CloudinaryService cloudinaryService;

    public AdminCategoryController(CategoryRepository categoryRepository, VehicleRepository vehicleRepository, CloudinaryService cloudinaryService) {
        this.categoryRepository = categoryRepository;
        this.vehicleRepository = vehicleRepository;
        this.cloudinaryService = cloudinaryService;
    }

    @PostMapping
    public ResponseEntity<AppApiResponse<Category>> create(
            @RequestParam String name,
            @RequestParam String description,
            @RequestParam(required = false) MultipartFile icon) {
        
        Category category = new Category();
        category.setName(name);
        category.setDescription(description);
        
        if (icon != null && !icon.isEmpty()) {
            category.setImage(cloudinaryService.upload(icon));
        }

        return ResponseEntity.ok(AppApiResponse.success("Tạo danh mục thành công", categoryRepository.save(category)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AppApiResponse<Category>> update(
            @PathVariable String id,
            @RequestParam String name,
            @RequestParam String description,
            @RequestParam(required = false) MultipartFile icon) {
        
        Category category = categoryRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục"));
        category.setName(name);
        category.setDescription(description);
        
        if (icon != null && !icon.isEmpty()) {
            // Xóa ảnh cũ nếu có
            if (category.getImage() != null) {
                cloudinaryService.delete(category.getImage());
            }
            category.setImage(cloudinaryService.upload(icon));
        }

        return ResponseEntity.ok(AppApiResponse.success("Cập nhật danh mục thành công", categoryRepository.save(category)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<AppApiResponse<Void>> delete(@PathVariable String id) {
        Category category = categoryRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục"));
        
        // Kiểm tra xem có xe nào đang dùng danh mục này không
        long count = vehicleRepository.countByCategoryId(id);
        if (count > 0) {
            throw new RuntimeException("Không thể xoá danh mục đang có " + count + " xe sử dụng. Vui lòng chuyển các xe sang danh mục khác trước.");
        }

        if (category.getImage() != null) {
            cloudinaryService.delete(category.getImage());
        }
        
        categoryRepository.delete(category);
        return ResponseEntity.ok(AppApiResponse.success("Đã xoá danh mục thành công"));
    }
}
