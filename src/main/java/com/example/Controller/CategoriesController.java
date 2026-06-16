package com.example.Controller;

import com.example.Entity.Categories;
import com.example.Repository.CategoriesRepository;
import com.example.Service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/study")
public class CategoriesController {

    @Autowired
    private CategoriesRepository categoryRepository;

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/categories")
    public String listCategories(Model model) {
        model.addAttribute("categories", categoryRepository.findByIsActiveTrue());
        model.addAttribute("currentPage", "categories");
        return "study/categories";
    }

    @GetMapping("/api/admin/categories/active")
    @ResponseBody
    public List<Categories> getActiveCategories() {
        return categoryRepository.findByIsActiveTrue();
    }


    /**
     * Lấy danh sách tất cả category (có thông tin next_id, total_tests, unlock_score)
     */
    @GetMapping("/api/admin/categories/all")
    @ResponseBody
    public List<Categories> getAllCategories() {
        return categoryService.getAllCategoriesOrdered();
    }

    /**
     * Lấy chi tiết 1 category
     */
    @GetMapping("/api/admin/categories/{id}")
    @ResponseBody
    public ResponseEntity<?> getCategoryById(@PathVariable Integer id) {
        Optional<Categories> category = categoryRepository.findById(id);
        if (category.isPresent()) {
            return ResponseEntity.ok(category.get());
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Thêm category mới (tự động cập nhật next_category_id)
     */
    @PostMapping("/api/admin/categories/create")
    @ResponseBody
    public ResponseEntity<?> createCategory(@RequestBody Categories category) {
        try {
            // Validate slug không trùng
            if (categoryRepository.findBySlug(category.getSlug()).isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Slug đã tồn tại"));
            }

            // Validate tên không trùng
            if (categoryRepository.existsByCategoryName(category.getCategoryName())) {
                return ResponseEntity.badRequest().body(Map.of("error", "Tên category đã tồn tại"));
            }

            Categories saved = categoryService.createCategory(category);
            return ResponseEntity.ok(Map.of(
                    "message", "Thêm category thành công",
                    "category", saved
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Cập nhật category
     */
    @PutMapping("/api/admin/categories/update/{id}")
    @ResponseBody
    public ResponseEntity<?> updateCategory(@PathVariable Integer id, @RequestBody Categories categoryUpdate) {
        try {
            Optional<Categories> existingOpt = categoryRepository.findById(id);
            if (existingOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Categories category = existingOpt.get();

            // Cập nhật các trường
            if (categoryUpdate.getCategoryName() != null) {
                category.setCategoryName(categoryUpdate.getCategoryName());
            }
            if (categoryUpdate.getSlug() != null) {
                // Kiểm tra slug không trùng (trừ chính nó)
                Optional<Categories> slugExist = categoryRepository.findBySlug(categoryUpdate.getSlug());
                if (slugExist.isPresent() && !slugExist.get().getId().equals(id)) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Slug đã tồn tại"));
                }
                category.setSlug(categoryUpdate.getSlug());
            }
            if (categoryUpdate.getIconUrl() != null) {
                category.setIconUrl(categoryUpdate.getIconUrl());
            }
            if (categoryUpdate.getJlptLevel() != null) {
                category.setJlptLevel(categoryUpdate.getJlptLevel());
            }
            if (categoryUpdate.getIsActive() != null) {
                category.setIsActive(categoryUpdate.getIsActive());
            }
            if (categoryUpdate.getThumbnailUrl() != null) {
                category.setThumbnailUrl(categoryUpdate.getThumbnailUrl());
            }
            if (categoryUpdate.getIsLocked() != null) {
                category.setIsLocked(categoryUpdate.getIsLocked());
            }
            if (categoryUpdate.getUnlockAvgScore() != null) {
                category.setUnlockAvgScore(categoryUpdate.getUnlockAvgScore());
            }

            Categories saved = categoryRepository.save(category);
            return ResponseEntity.ok(Map.of(
                    "message", "Cập nhật category thành công",
                    "category", saved
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Xóa category (tự động cập nhật lại next_category_id)
     */
    @DeleteMapping("/api/admin/categories/delete/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteCategory(@PathVariable Integer id) {
        try {
            Optional<Categories> category = categoryRepository.findById(id);
            if (category.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            categoryService.deleteCategory(id);
            return ResponseEntity.ok(Map.of("message", "Xóa category thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Refresh toàn bộ next_category_id (dùng khi dữ liệu bị lỗi)
     */
    @PostMapping("/api/admin/categories/refresh-next-ids")
    @ResponseBody
    public ResponseEntity<?> refreshNextIds() {
        try {
            categoryService.refreshAllNextCategoryIds();
            return ResponseEntity.ok(Map.of("message", "Đã cập nhật next_category_id cho tất cả category"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Lấy category tiếp theo
     */
    @GetMapping("/api/categories/{id}/next")
    @ResponseBody
    public ResponseEntity<?> getNextCategory(@PathVariable Integer id) {
        Categories next = categoryService.getNextCategory(id);
        if (next != null) {
            return ResponseEntity.ok(next);
        }
        return ResponseEntity.ok(Map.of("message", "Đây là category cuối cùng"));
    }

    /**
     * Kiểm tra điều kiện mở khóa category
     */
    @GetMapping("/api/categories/{id}/can-unlock")
    @ResponseBody
    public ResponseEntity<?> checkCanUnlock(@PathVariable Integer id, @RequestParam Double userAvgScore) {
        boolean canUnlock = categoryService.canUnlockCategory(id, userAvgScore);
        return ResponseEntity.ok(Map.of(
                "canUnlock", canUnlock,
                "categoryId", id,
                "requiredScore", categoryRepository.findById(id).map(Categories::getUnlockAvgScore).orElse(70.0),
                "userScore", userAvgScore
        ));
    }

    /**
     * Cập nhật total_tests và unlock_avg_score cho TẤT CẢ category
     */
    @PostMapping("/api/admin/categories/update-all-stats")
    @ResponseBody
    public ResponseEntity<?> updateAllCategoriesStats() {
        try {
            categoryService.updateAllCategoriesStats();
            return ResponseEntity.ok(Map.of("message", "Đã cập nhật stats cho tất cả category"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

}
