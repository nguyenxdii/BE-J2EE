package com.j2ee.carbooking.controller;

import com.j2ee.carbooking.model.Category;
import com.j2ee.carbooking.repository.CategoryRepository;
import com.j2ee.carbooking.repository.VehicleRepository;
import com.j2ee.carbooking.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryRepository categoryRepository;
    private final VehicleRepository vehicleRepository;
    private final CloudinaryService cloudinaryService;

    @GetMapping
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(consumes = {"multipart/form-data"})
    public Category createCategory(
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestPart(required = false) MultipartFile icon
    ) {
        if (categoryRepository.findByName(name.trim()).isPresent()) {
            throw new RuntimeException("Danh mục đã tồn tại");
        }
        Category category = new Category();
        category.setName(name.trim());
        category.setDescription(description);
        if (icon != null && !icon.isEmpty()) {
            category.setImage(cloudinaryService.uploadImage(icon, "categories"));
        }
        return categoryRepository.save(category);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    public Category updateCategory(
            @PathVariable String id,
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestPart(required = false) MultipartFile icon
    ) {
        Category existing = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục"));
        existing.setName(name.trim());
        existing.setDescription(description);
        if (icon != null && !icon.isEmpty()) {
            cloudinaryService.deleteByUrl(existing.getImage());
            existing.setImage(cloudinaryService.uploadImage(icon, "categories"));
        }
        return categoryRepository.save(existing);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public Map<String, Object> deleteCategory(@PathVariable String id) {
        long totalVehiclesUsingCategory = vehicleRepository.countByCategoryId(id);
        if (totalVehiclesUsingCategory > 0) {
            throw new RuntimeException("Không thể xóa danh mục vì đang có " + totalVehiclesUsingCategory + " xe sử dụng");
        }
        Category existing = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục"));
        cloudinaryService.deleteByUrl(existing.getImage());
        categoryRepository.deleteById(id);
        return Map.of("deleted", true);
    }
}
