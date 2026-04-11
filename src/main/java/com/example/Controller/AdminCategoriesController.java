package com.example.Controller;

import com.example.Entity.Categories;
import com.example.Repository.CategoriesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController // Trả về JSON cho JavaScript fetch
@RequestMapping("/api/admin/categories")
public class AdminCategoriesController {

    @Autowired
    private CategoriesRepository categoriesRepository;

    // 1. Lấy danh sách cho bảng Admin
    @GetMapping
    public ResponseEntity<List<Categories>> getAll() {
        return ResponseEntity.ok(categoriesRepository.findAll());
    }

    // 2. Thêm mới danh mục (Từ Modal SweetAlert2 gửi lên)
    @PostMapping
    public ResponseEntity<?> add(@RequestBody Categories category) {
        if (categoriesRepository.existsByCategoryName(category.getCategoryName())) {
            return ResponseEntity.badRequest().body("Tên danh mục đã tồn tại!");
        }
        // Thiết lập các giá trị mặc định nếu cần
        if (category.getProgress() == null) category.setProgress(0);
        if (category.getTotalLessons() == null) category.setTotalLessons(0);

        return ResponseEntity.ok(categoriesRepository.save(category));
    }

    // 3. Xóa danh mục
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        if (!categoriesRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        categoriesRepository.deleteById(id);
        return ResponseEntity.ok().body("Đã xóa danh mục thành công");
    }
}