package com.example.Controller;

import com.example.Entity.Lessons;
import com.example.Repository.LessonsRepository;
import com.example.Repository.CategoriesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
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

    // 1. Lấy tất cả bài học (Kèm tên danh mục) - GIỮ LẠI cho các chức năng cũ
    @GetMapping
    public ResponseEntity<?> getAllLessons() {
        List<Object[]> rows = lessonRepository.findAllLessonsWithStats();
        List<Map<String, Object>> response = rows.stream().map(row -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", row[0]);
            item.put("lessonName", row[1]);
            item.put("categoryName", row[2] != null ? row[2] : "N/A");
            item.put("totalVocab", row[3] != null ? row[3] : 0);
            return item;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    // 1.1 API MỚI: Lấy danh sách lessons có phân trang và filter
    @GetMapping("/paging")
    public ResponseEntity<Page<Map<String, Object>>> getLessonsPaging(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) String level,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        String cleanSearch = (search != null && search.isEmpty()) ? null : search;
        String cleanLevel = (level != null && level.isEmpty()) ? null : level;
        Pageable pageable = PageRequest.of(page, size);

        Page<Object[]> resultPage = lessonRepository.findLessonsWithStatsByFilters(cleanSearch, categoryId, cleanLevel, pageable);

        List<Map<String, Object>> lessons = resultPage.getContent().stream().map(row -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", row[0]);
            item.put("lessonName", row[1]);
            item.put("categoryId", row[2]);
            item.put("categoryName", row[3] != null ? row[3] : "Chưa phân loại");
            item.put("totalVocab", row[4] != null ? row[4] : 0L);
            // THÊM DÒNG NÀY - lấy isActive từ cột thứ 6 (index 5)
            item.put("isActive", row[5] != null ? (Boolean) row[5] : true);
            return item;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(new PageImpl<>(lessons, pageable, resultPage.getTotalElements()));
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

    // 3. SOFT DELETE - Ẩn/hiện bài học (toggle isActive)
    @PatchMapping("/{id}/toggle-status")
    @Transactional
    public ResponseEntity<?> toggleLessonStatus(@PathVariable Integer id) {
        return lessonRepository.findById(id)
                .map(lesson -> {
                    boolean newStatus = !lesson.getIsActive();
                    lesson.setIsActive(newStatus);
                    lessonRepository.save(lesson);

                    String message = newStatus ? "Đã khôi phục bài học" : "Đã ẩn bài học";
                    return ResponseEntity.ok().body(message);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // 4. Lọc bài học theo level (giữ nguyên)
    @GetMapping("/filter")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getLessonsByLevel(@RequestParam(required = false) String level) {
        List<Lessons> lessons;
        if (level == null || level.isEmpty()) {
            lessons = lessonRepository.findAll();
        } else {
            lessons = lessonRepository.findByCategory_JlptLevel(level);
        }

        List<Map<String, Object>> response = lessons.stream().map(lesson -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", lesson.getId());
            item.put("lessonName", lesson.getLessonName());
            return item;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    // 5. Lấy bài học theo category (có kèm trạng thái)
    @GetMapping("/by-category/{catId}")
    public ResponseEntity<List<Map<String, Object>>> getByCate(@PathVariable Integer catId) {
        List<Object[]> rows = lessonRepository.findLessonsByCategoryIdWithStats(catId);
        List<Map<String, Object>> response = rows.stream().map(row -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", row[0]);
            item.put("lessonName", row[1]);
            item.put("isActive", row[2]);
            item.put("totalVocab", row[3] != null ? row[3] : 0);
            return item;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
}