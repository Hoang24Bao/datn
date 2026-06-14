package com.example.Controller;

import com.example.Entity.Categories;
import com.example.Repository.CategoriesRepository;
import com.example.Repository.LessonsRepository;
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
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/api/admin/categories")
public class AdminCategoriesController {

    @Autowired
    private CategoriesRepository categoriesRepository;

    @Autowired
    private LessonsRepository lessonsRepository;


    @GetMapping
    public ResponseEntity<List<Categories>> getAll() {
        return ResponseEntity.ok(categoriesRepository.findAll());
    }

    //1 Lấy danh sách categories
    @GetMapping("/paging")
    public ResponseEntity<Page<Categories>> getCategoriesPaging(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        String cleanSearch = (search != null && search.isEmpty()) ? null : search;
        String cleanLevel = (level != null && level.isEmpty()) ? null : level;
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        Page<Categories> categoriesPage = categoriesRepository.findByFilters(cleanSearch, cleanLevel, isActive, pageable);
        return ResponseEntity.ok(categoriesPage);
    }

    //2. Thêm mới cate
    @PostMapping("/add-with-file")
    @Transactional
    public ResponseEntity<?> addCategoryWithFile(
            @RequestParam("categoryName") String categoryName,
            @RequestParam("jlptLevel") String jlptLevel,
            @RequestParam(value = "iconUrl", required = false) String iconUrl,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "thumbnailFile", required = false) MultipartFile thumbnailFile) {
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
            String finalThumbnailUrl = null;

            if (file != null && !file.isEmpty()) {
                String uploadDir = "src/main/resources/static/img/categories/icon/";
                File dir = new File(uploadDir);
                if (!dir.exists()) dir.mkdirs();

                String originalName = file.getOriginalFilename();
                String extension = (originalName != null && originalName.contains("."))
                        ? originalName.substring(originalName.lastIndexOf("."))
                        : ".jpg";

                String fileName = "category-" + savedCategory.getId() + extension;
                Path filePath = Paths.get(uploadDir + fileName);
                Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                finalIconUrl = "img/categories/icon/" + fileName;
            }

            if (thumbnailFile != null && !thumbnailFile.isEmpty()) {
                String thumbnailDir = "src/main/resources/static/img/categories/thumbnail/";
                File thumbnailDirFile = new File(thumbnailDir);
                if (!thumbnailDirFile.exists()) thumbnailDirFile.mkdirs();

                String originalName = thumbnailFile.getOriginalFilename();
                String extension = (originalName != null && originalName.contains("."))
                        ? originalName.substring(originalName.lastIndexOf("."))
                        : ".jpg";

                String fileName = savedCategory.getId() + extension;
                Path filePath = Paths.get(thumbnailDir + fileName);
                Files.copy(thumbnailFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                finalThumbnailUrl = "img/categories/thumbnail/" + fileName;
            }

            savedCategory.setIconUrl(finalIconUrl);
            savedCategory.setThumbnailUrl(finalThumbnailUrl);
            categoriesRepository.save(savedCategory);

            return ResponseEntity.ok(savedCategory);

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Lỗi hệ thống khi lưu file: " + e.getMessage());
        }
    }

    //3. Xóa cate
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

    //4. Lọc cate theo Level
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

    //7. Upload icon cho category
    @PostMapping("/upload-icon")
    @Transactional
    public ResponseEntity<?> uploadCategoryIcon(
            @RequestParam("file") MultipartFile file,
            @RequestParam("categoryId") Integer categoryId) {
        try {
            Categories category = categoriesRepository.findById(categoryId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy chủ đề"));

            String uploadDir = "src/main/resources/static/img/categories/icon/";
            File dir = new File(uploadDir);
            if (!dir.exists()) dir.mkdirs();

            String originalName = file.getOriginalFilename();
            String extension = (originalName != null && originalName.contains("."))
                    ? originalName.substring(originalName.lastIndexOf("."))
                    : ".jpg";

            String fileName = "category-" + categoryId + extension;
            Path filePath = Paths.get(uploadDir + fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            String iconUrl = "img/categories/icon/" + fileName;
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

    @PostMapping("/upload-thumbnail")
    @Transactional
    public ResponseEntity<?> uploadCategoryThumbnail(
            @RequestParam("thumbnailFile") MultipartFile file,
            @RequestParam("categoryId") Integer categoryId) {
        try {
            Categories category = categoriesRepository.findById(categoryId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy chủ đề"));

            String uploadDir = "src/main/resources/static/img/categories/thumbnail/";
            File dir = new File(uploadDir);
            if (!dir.exists()) dir.mkdirs();

            String originalName = file.getOriginalFilename();
            String extension = (originalName != null && originalName.contains("."))
                    ? originalName.substring(originalName.lastIndexOf("."))
                    : ".jpg";

            String fileName = categoryId + extension;
            Path filePath = Paths.get(uploadDir + fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            String thumbnailUrl = "img/categories/thumbnail/" + fileName;
            category.setThumbnailUrl(thumbnailUrl);
            categoriesRepository.save(category);

            Map<String, String> response = new HashMap<>();
            response.put("thumbnailUrl", thumbnailUrl);
            response.put("message", "Upload thumbnail thành công");
            return ResponseEntity.ok(response);

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Lỗi hệ thống khi upload file: " + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }


    private String generateSlug(String input) {
        if (input == null) return "";

        String slug = input.trim().toLowerCase();

        slug = slug
                .replaceAll("[áàảãạăắằẳẵặâấầẩẫậ]", "a")
                .replaceAll("[éèẻẽẹêếềểễệ]", "e")
                .replaceAll("[íìỉĩị]", "i")
                .replaceAll("[óòỏõọôốồổỗộơớờởỡợ]", "o")
                .replaceAll("[úùủũụưứừửữự]", "u")
                .replaceAll("[ýỳỷỹỵ]", "y")
                .replaceAll("đ", "d");

        slug = slug.replaceAll("\\s+", "-");
        slug = slug.replaceAll("[^a-z0-9-]", "");
        slug = slug.replaceAll("^-+|-+$", "");
        slug = slug.replaceAll("-+", "-");
        return slug;
    }


    // 1. API lấy danh sách categories cho dropdown
    @GetMapping("/active")
    public ResponseEntity<List<Map<String, Object>>> getActiveCategoriesForDropdown() {
        List<Categories> categories = categoriesRepository.findByIsActiveTrue();

        List<Map<String, Object>> result = categories.stream()
                .map(cat -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", cat.getId());
                    map.put("categoryName", cat.getCategoryName());
                    map.put("jlptLevel", cat.getJlptLevel());
                    return map;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    // 2. API lấy chi tiết category
    @GetMapping("/{id}/detail")
    public ResponseEntity<Map<String, Object>> getCategoryDetail(@PathVariable Integer id) {
        return categoriesRepository.findById(id)
                .map(category -> {
                    Map<String, Object> detail = new HashMap<>();
                    detail.put("id", category.getId());
                    detail.put("categoryName", category.getCategoryName());
                    detail.put("slug", category.getSlug());
                    detail.put("jlptLevel", category.getJlptLevel());
                    detail.put("iconUrl", category.getIconUrl());
                    detail.put("thumbnailUrl", category.getThumbnailUrl());
                    detail.put("isActive", category.getIsActive());

                    int lessonCount = lessonsRepository.countActiveByCategory(id);
                    detail.put("lessonCount", lessonCount);

                    return ResponseEntity.ok(detail);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}