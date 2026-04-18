package com.example.Controller;

import com.example.Entity.Categories;
import com.example.Repository.CategoriesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/api/admin/categories")
public class AdminCategoriesController {

    @Autowired
    private CategoriesRepository categoriesRepository;

    //1. Lấy danh sách cho bảng Admin
    @GetMapping
    public ResponseEntity<List<Categories>> getAll() {
        return ResponseEntity.ok(categoriesRepository.findAll());
    }

    //1.1 Lấy danh sách categories có phân trang và filter
    @GetMapping("/paging")
    public ResponseEntity<Page<Categories>> getCategoriesPaging(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        String cleanSearch = (search != null && search.isEmpty()) ? null : search;
        String cleanLevel = (level != null && level.isEmpty()) ? null : level;
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        Page<Categories> categoriesPage = categoriesRepository.findByFilters(cleanSearch, cleanLevel, isActive, pageable);
        return ResponseEntity.ok(categoriesPage);
    }

    //2. Thêm mới danh mục kèm xử lý file ảnh
    @PostMapping("/add-with-file")
    @Transactional
    public ResponseEntity<?> addCategoryWithFile(
            @RequestParam("categoryName") String categoryName,
            @RequestParam("jlptLevel") String jlptLevel,
            @RequestParam(value = "iconUrl", required = false) String iconUrl,
            @RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            if (categoriesRepository.existsByCategoryName(categoryName)) {
                return ResponseEntity.badRequest().body("Tên chủ đề đã tồn tại!");
            }

            Categories category = new Categories();
            category.setCategoryName(categoryName);
            category.setJlptLevel(jlptLevel);
            category.setSlug(generateSlug(categoryName));
            category.setIsActive(true);
            category.setTotalLessons(0);
            category.setProgress(0);

            Categories savedCategory = categoriesRepository.save(category);
            String finalIconUrl = iconUrl;

            if (file != null && !file.isEmpty()) {
                String uploadDir = "src/main/resources/static/img/categories/";
                File dir = new File(uploadDir);
                if (!dir.exists()) dir.mkdirs();

                String originalName = file.getOriginalFilename();
                String extension = (originalName != null && originalName.contains("."))
                        ? originalName.substring(originalName.lastIndexOf("."))
                        : ".jpg";

                String fileName = "category-" + savedCategory.getId() + extension;
                Path filePath = Paths.get(uploadDir + fileName);
                Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                finalIconUrl = "img/categories/" + fileName;
            }

            savedCategory.setIconUrl(finalIconUrl);
            return ResponseEntity.ok(categoriesRepository.save(savedCategory));

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Lỗi hệ thống khi lưu file: " + e.getMessage());
        }
    }

    //3. Xóa (ẩn/hiện) danh mục
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> toggleCategoryStatus(@PathVariable Integer id) {
        return categoriesRepository.findById(id)
                .map(category -> {
                    boolean newStatus = !category.getIsActive();
                    category.setIsActive(newStatus);
                    categoriesRepository.save(category);
                    String message = newStatus ? "Đã khôi phục chủ đề" : "Đã ẩn chủ đề";
                    return ResponseEntity.ok().body(message);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    //4. Lọc danh mục theo Level
    @GetMapping("/filter")
    public ResponseEntity<?> getCategoriesByLevel(@RequestParam String level) {
        List<Categories> categories = categoriesRepository.findByJlptLevel(level);
        return ResponseEntity.ok(categories);
    }

    //5. Lấy chi tiết category theo id
    @GetMapping("/{id}")
    public ResponseEntity<Categories> getCategoryById(@PathVariable Integer id) {
        return categoriesRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    //6. Cập nhật category
    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<?> updateCategory(@PathVariable Integer id, @RequestBody Categories category) {
        return categoriesRepository.findById(id)
                .map(existingCategory -> {
                    if (category.getCategoryName() != null &&
                            !existingCategory.getCategoryName().equals(category.getCategoryName()) &&
                            categoriesRepository.existsByCategoryName(category.getCategoryName())) {
                        return ResponseEntity.badRequest().body("Tên chủ đề đã tồn tại!");
                    }

                    if (category.getCategoryName() != null) {
                        existingCategory.setCategoryName(category.getCategoryName());
                        existingCategory.setSlug(generateSlug(category.getCategoryName()));
                    }
                    if (category.getJlptLevel() != null) {
                        existingCategory.setJlptLevel(category.getJlptLevel());
                    }
                    if (category.getIconUrl() != null) {
                        existingCategory.setIconUrl(category.getIconUrl());
                    }
                    if (category.getIsActive() != null) {
                        existingCategory.setIsActive(category.getIsActive());
                    }
                    if (category.getProgress() != null) {
                        existingCategory.setProgress(category.getProgress());
                    }

                    categoriesRepository.save(existingCategory);
                    return ResponseEntity.ok(existingCategory);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    //7. Upload icon riêng cho category
    @PostMapping("/upload-icon")
    @Transactional
    public ResponseEntity<?> uploadCategoryIcon(
            @RequestParam("file") MultipartFile file,
            @RequestParam("categoryId") Integer categoryId) {
        try {
            Categories category = categoriesRepository.findById(categoryId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy chủ đề"));

            String uploadDir = "src/main/resources/static/img/categories/";
            File dir = new File(uploadDir);
            if (!dir.exists()) dir.mkdirs();

            String originalName = file.getOriginalFilename();
            String extension = (originalName != null && originalName.contains("."))
                    ? originalName.substring(originalName.lastIndexOf("."))
                    : ".jpg";

            String fileName = "category-" + categoryId + extension;
            Path filePath = Paths.get(uploadDir + fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            String iconUrl = "img/categories/" + fileName;
            category.setIconUrl(iconUrl);
            categoriesRepository.save(category);

            Map<String, String> response = new HashMap<>();
            response.put("iconUrl", iconUrl);
            response.put("message", "Upload ảnh thành công");
            return ResponseEntity.ok(response);

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Lỗi hệ thống khi upload file: " + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }


    // Hàm hỗ trợ tạo Slug từ tên tiếng Việt
    private String generateSlug(String input) {
        if (input == null) return "";

        // Bước 1: Trim khoảng trắng đầu và cuối
        String slug = input.trim().toLowerCase();

        // Bước 2: Thay thế các ký tự có dấu thành không dấu
        slug = slug
                .replaceAll("[áàảãạăắằẳẵặâấầẩẫậ]", "a")
                .replaceAll("[éèẻẽẹêếềểễệ]", "e")
                .replaceAll("[íìỉĩị]", "i")
                .replaceAll("[óòỏõọôốồổỗộơớờởỡợ]", "o")
                .replaceAll("[úùủũụưứừửữự]", "u")
                .replaceAll("[ýỳỷỹỵ]", "y")
                .replaceAll("đ", "d");

        // Bước 3: Thay thế 1 hoặc nhiều khoảng trắng bằng 1 dấu gạch ngang
        slug = slug.replaceAll("\\s+", "-");

        // Bước 4: Loại bỏ các ký tự không phải chữ cái, số, dấu gạch ngang
        slug = slug.replaceAll("[^a-z0-9-]", "");

        // Bước 5: Quan trọng - Loại bỏ dấu gạch ngang ở đầu và cuối
        slug = slug.replaceAll("^-+|-+$", "");

        // Bước 6: Thay thế nhiều dấu gạch ngang liên tiếp bằng 1 dấu
        slug = slug.replaceAll("-+", "-");

        return slug;
    }
}