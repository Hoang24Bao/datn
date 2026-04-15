package com.example.Controller;

import com.example.Entity.Categories;
import com.example.Repository.CategoriesRepository;
import org.springframework.beans.factory.annotation.Autowired;
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

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RequestMapping("/api/admin/categories")
public class AdminCategoriesController {

    @Autowired
    private CategoriesRepository categoriesRepository;

    /**
     * 1. Lấy danh sách cho bảng Admin
     */
    @GetMapping
    public ResponseEntity<List<Categories>> getAll() {
        return ResponseEntity.ok(categoriesRepository.findAll());
    }

    /**
     * 2. Thêm mới danh mục kèm xử lý file ảnh (Phương án tối ưu nhất)
     * Quy trình: Lưu DB nháp -> Lấy ID -> Đổi tên file theo ID -> Lưu file -> Cập nhật lại DB
     */
    @PostMapping("/add-with-file")
    @Transactional // Đảm bảo nếu lưu file lỗi thì DB sẽ rollback (hủy bản ghi nháp)
    public ResponseEntity<?> addCategoryWithFile(
            @RequestParam("categoryName") String categoryName,
            @RequestParam("jlptLevel") String jlptLevel,
            @RequestParam(value = "iconUrl", required = false) String iconUrl,
            @RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            // Bước 0: Kiểm tra trùng tên
            if (categoriesRepository.existsByCategoryName(categoryName)) {
                return ResponseEntity.badRequest().body("Tên chủ đề đã tồn tại!");
            }

            // Bước 1: Tạo đối tượng và lưu lần 1 để lấy ID tự tăng (Identity)
            Categories category = new Categories();
            category.setCategoryName(categoryName);
            category.setJlptLevel(jlptLevel);
            category.setSlug(generateSlug(categoryName));
            category.setIsActive(true);
            category.setTotalLessons(0);
            category.setProgress(0);

            // Lưu để lấy ID
            Categories savedCategory = categoriesRepository.save(category);

            // Bước 2: Xử lý lưu file ảnh
            String finalIconUrl = iconUrl; // Mặc định lấy link nhập tay nếu không có file upload

            if (file != null && !file.isEmpty()) {
                String uploadDir = "src/main/resources/static/img/categories/";
                File dir = new File(uploadDir);
                if (!dir.exists()) dir.mkdirs();

                // Lấy phần mở rộng của file (vd: .jpg, .png)
                String originalName = file.getOriginalFilename();
                String extension = (originalName != null && originalName.contains("."))
                        ? originalName.substring(originalName.lastIndexOf("."))
                        : ".jpg";

                // Đổi tên file theo quy tắc: category-{id}.jpg
                String fileName = "category-" + savedCategory.getId() + extension;
                Path filePath = Paths.get(uploadDir + fileName);

                // Lưu file vật lý vào thư mục project
                Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                finalIconUrl = "img/categories/" + fileName;
            }

            // Bước 3: Cập nhật lại iconUrl chính thức và lưu lần cuối
            savedCategory.setIconUrl(finalIconUrl);
            return ResponseEntity.ok(categoriesRepository.save(savedCategory));

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Lỗi hệ thống khi lưu file: " + e.getMessage());
        }
    }

    /**
     * 3. Xóa danh mục
     */
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> toggleCategoryStatus(@PathVariable Integer id) {
        return categoriesRepository.findById(id)
                .map(category -> {
                    // Đảo ngược trạng thái isActive
                    boolean newStatus = !category.getIsActive();
                    category.setIsActive(newStatus);
                    categoriesRepository.save(category);

                    String message = newStatus ? "Đã khôi phục chủ đề" : "Đã ẩn chủ đề";
                    return ResponseEntity.ok().body(message);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 4. Lọc danh mục theo Level
     */
    @GetMapping("/filter")
    public ResponseEntity<?> getCategoriesByLevel(@RequestParam String level) {
        List<Categories> categories = categoriesRepository.findByJlptLevel(level);
        return ResponseEntity.ok(categories);
    }

    /**
     * Hàm hỗ trợ tạo Slug từ tên tiếng Việt
     */
    private String generateSlug(String input) {
        if (input == null) return "";
        return input.toLowerCase()
                .replaceAll("[áàảãạăắằẳẵặâấầẩẫậ]", "a")
                .replaceAll("[éèẻẽẹêếềểễệ]", "e")
                .replaceAll("[íìỉĩị]", "i")
                .replaceAll("[óòỏõọôốồổỗộơớờởỡợ]", "o")
                .replaceAll("[úùủũụưứừửữự]", "u")
                .replaceAll("[ýỳỷỹỵ]", "y")
                .replaceAll("đ", "d")
                .replaceAll("[^a-z0-9\\s]", "")
                .replaceAll("\\s+", "-")
                .trim();
    }
}