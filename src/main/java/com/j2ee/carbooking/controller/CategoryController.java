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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryRepository categoryRepository;
    private final VehicleRepository vehicleRepository;
    private final CloudinaryService cloudinaryService;

    @GetMapping
    public List<Category> getAllCategories() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return categoryRepository.findAll();
        }
        return categoryRepository.findByHiddenFalse();
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

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/toggle-hide")
    public Category toggleHideCategory(@PathVariable String id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục"));
        category.setHidden(!category.isHidden());
        return categoryRepository.save(category);
    }
}
