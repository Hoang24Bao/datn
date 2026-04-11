package com.example.Controller;

import com.example.Entity.Lessons;
import com.example.Repository.LessonsRepository;
import com.example.Repository.CategoriesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/lessons")
public class AdminLessonController {

    @Autowired
    private LessonsRepository lessonRepository;

    @Autowired
    private CategoriesRepository categoryRepository;

    // 1. Lấy tất cả bài học (Kèm tên danh mục)
    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<?> getAllLessons() {
        List<Lessons> lessons = lessonRepository.findAll();
        List<Map<String, Object>> response = lessons.stream().map(lesson -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", lesson.getId());
            item.put("lessonName", lesson.getLessonName());
            item.put("categoryName", lesson.getCategory() != null ? lesson.getCategory().getCategoryName() : "N/A");
            item.put("totalVocab", lesson.getVocabularies() != null ? lesson.getVocabularies().size() : 0);
            return item;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    // 2. Thêm bài học mới
    @PostMapping
    public ResponseEntity<?> createLesson(@RequestBody Map<String, Object> payload) {
        try {
            String name = (String) payload.get("lessonName");
            // Chuyển đổi ID an toàn từ payload
            Integer categoryId = Integer.parseInt(payload.get("categoryId").toString());

            Lessons lesson = new Lessons();
            lesson.setLessonName(name);
            lesson.setCategoryId(categoryId);
            lesson.setFree(true);
            lesson.setOrderIndex(0);

            lessonRepository.save(lesson);
            return ResponseEntity.ok("Thêm bài học thành công!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }

    // 3. Xóa bài học (Bổ sung để khớp với giao diện)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteLesson(@PathVariable Integer id) {
        try {
            if (!lessonRepository.existsById(id)) {
                return ResponseEntity.notFound().build();
            }
            lessonRepository.deleteById(id);
            return ResponseEntity.ok("Xóa bài học thành công!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Không thể xóa bài học vì có dữ liệu liên quan!");
        }
    }
}