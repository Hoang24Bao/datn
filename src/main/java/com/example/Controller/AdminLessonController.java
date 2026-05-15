package com.example.Controller;

import com.example.Dto.Response.InteractiveSceneDTO;
import com.example.Entity.Categories;
import com.example.Entity.InteractiveScene;
import com.example.Entity.Lessons;
import com.example.Entity.Vocabulary;
import com.example.Repository.InteractiveSceneRepository;
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
    private InteractiveSceneRepository interactiveSceneRepository;

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
            @RequestParam(defaultValue = "30") int size) {

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
            item.put("isActive", row[5] != null ? (Boolean) row[5] : true);
            return item;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(new PageImpl<>(lessons, pageable, resultPage.getTotalElements()));
    }

    // 2. Thêm bài học mới (từ trang Lessons - chọn cấp độ/chủ đề/trạng thái)
    @PostMapping
    @Transactional
    public ResponseEntity<?> createLesson(@RequestBody Map<String, Object> payload) {
        try {
            String name = (String) payload.get("lessonName");
            Integer categoryId = Integer.parseInt(payload.get("categoryId").toString());
            Boolean isActive = payload.get("isActive") != null
                    ? Boolean.parseBoolean(payload.get("isActive").toString())
                    : true;
            Categories category = categoryRepository.findById(categoryId).orElse(null);

            if (lessonRepository.countByLessonNameAndCategoryId(name, categoryId) > 0) {
                return ResponseEntity.badRequest().body("Tên bài học đã tồn tại trong chủ đề này!");
            }

            Lessons lesson = new Lessons();
            lesson.setLessonName(name);
            lesson.setCategoryId(categoryId);
            lesson.setIsActive(isActive);
            lesson.setFree(true);


            // Tính order_index: đếm số lesson hiện có trong category + 1
            int orderIndex = lessonRepository.countByCategoryId(categoryId);
            lesson.setOrderIndex(orderIndex + 1);

            lessonRepository.save(lesson);

            // Cập nhật total_lessons trong bảng Categories
            syncTotalLessons(categoryId);

            return ResponseEntity.ok("Thêm bài học thành công!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }

    // 2.1 Thêm bài học mới từ trang chi tiết Category (chỉ cần tên + trạng thái)
    @PostMapping("/in-category/{catId}")
    @Transactional
    public ResponseEntity<?> createLessonInCategory(
            @PathVariable Integer catId,
            @RequestBody Map<String, Object> payload) {
        try {
            String name = (String) payload.get("lessonName");
            if (name == null || name.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Tên bài học không được để trống!");
            }

            Boolean isActive = payload.get("isActive") != null
                    ? Boolean.parseBoolean(payload.get("isActive").toString())
                    : true;

            Categories category = categoryRepository.findById(catId).orElse(null);

            // Kiểm tra category tồn tại
            if (!categoryRepository.existsById(catId)) {
                return ResponseEntity.badRequest().body("Không tìm thấy chủ đề!");
            }

            if (lessonRepository.countByLessonNameAndCategoryId(name.trim(), catId) > 0) {
                return ResponseEntity.badRequest().body("Tên bài học đã tồn tại trong chủ đề này!");
            }

            Lessons lesson = new Lessons();
            lesson.setLessonName(name.trim());
            lesson.setCategoryId(catId);
            lesson.setIsActive(isActive);
            lesson.setFree(true);

            // Tính order_index
            int orderIndex = lessonRepository.countByCategoryId(catId);
            lesson.setOrderIndex(orderIndex + 1);

            lessonRepository.save(lesson);

            // Cập nhật total_lessons trong bảng Categories
            syncTotalLessons(catId);

            return ResponseEntity.ok("Thêm bài học thành công!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }


    // 3. Cập nhật thông tin bài học
    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<?> updateLesson(@PathVariable Integer id, @RequestBody Map<String, Object> payload) {
        return lessonRepository.findById(id)
                .map(lesson -> {
                    try {
                        String name = (String) payload.get("lessonName");
                        if (name == null || name.trim().isEmpty()) {
                            return ResponseEntity.badRequest().body("Tên bài học không được để trống!");
                        }

                        Integer categoryId = payload.get("categoryId") != null
                                ? Integer.parseInt(payload.get("categoryId").toString())
                                : lesson.getCategoryId();

                        // Kiểm tra tên trùng trong cùng category (bỏ qua chính nó)
                        int dupCount = lessonRepository.countByLessonNameAndCategoryIdExcludeId(name.trim(), categoryId, id);
                        if (dupCount > 0) {
                            return ResponseEntity.badRequest().body("Tên bài học đã tồn tại trong chủ đề này!");
                        }

                        Integer oldCategoryId = lesson.getCategoryId();

                        lesson.setLessonName(name.trim());
                        lesson.setCategoryId(categoryId);

                        if (payload.get("isActive") != null) {
                            lesson.setIsActive(Boolean.parseBoolean(payload.get("isActive").toString()));
                        }

                        lessonRepository.save(lesson);

                        // Sync total_lessons cho category cũ và mới (nếu đổi category)
                        syncTotalLessons(categoryId);
                        if (!categoryId.equals(oldCategoryId)) {
                            syncTotalLessons(oldCategoryId);
                        }

                        return ResponseEntity.ok("Cập nhật bài học thành công!");
                    } catch (Exception e) {
                        return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
                    }
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // 4. SOFT DELETE - Ẩn/hiện bài học (toggle isActive)
    @PatchMapping("/{id}/toggle-status")
    @Transactional
    public ResponseEntity<?> toggleLessonStatus(@PathVariable Integer id) {
        return lessonRepository.findById(id)
                .map(lesson -> {
                    boolean newStatus = !lesson.getIsActive();
                    lesson.setIsActive(newStatus);
                    lessonRepository.save(lesson);

                    // Cập nhật total_lessons (chỉ đếm bài đang active)
                    syncTotalLessons(lesson.getCategoryId());

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


    @GetMapping("/{id}")
    public ResponseEntity<Lessons> getLessonById(@PathVariable Integer id) {
        return lessonRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    @GetMapping("/{id}/scenes")
    public ResponseEntity<?> getScenesByLesson(@PathVariable Integer id) {
        // Lấy lesson để biết categoryId
        return lessonRepository.findById(id).map(lesson -> {
            // Lấy scenes theo categoryId (không phải theo lessonId)
            List<InteractiveScene> scenes = interactiveSceneRepository.findByCategoryIdOrderByOrderIndexAsc(lesson.getCategoryId());

            List<InteractiveSceneDTO> response = scenes.stream()
                    .map(scene -> new InteractiveSceneDTO(
                            scene.getId(),
                            scene.getImageUrl(),
                            scene.getDescription(),
                            scene.getOrderIndex(),
                            scene.getPoints() != null ? scene.getPoints().size() : 0
                    ))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(response);
        }).orElse(ResponseEntity.notFound().build());
    }


    // Thêm từ vựng vào bài học
    @PostMapping("/lesson-vocab")
    @Transactional
    public ResponseEntity<?> addVocabToLesson(
            @RequestParam Integer lessonId,
            @RequestParam Integer vocabId,
            @RequestParam(required = false) Integer displayOrder) {
        try {
            boolean exists = lessonRepository.isVocabInLesson(lessonId, vocabId);
            if (exists) {
                return ResponseEntity.badRequest().body("Từ vựng đã có trong bài học này!");
            }
            if (displayOrder == null) {
                Integer maxOrder = lessonRepository.getMaxDisplayOrder(lessonId);
                displayOrder = (maxOrder != null ? maxOrder + 1 : 1);
            }

            lessonRepository.addVocabToLesson(lessonId, vocabId, displayOrder);
            return ResponseEntity.ok("Đã thêm từ vựng vào bài học");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }

    // Lấy danh sách từ vựng CHƯA có trong bài học (để thêm)
    @GetMapping("/{lessonId}/available-vocab")
    public ResponseEntity<?> getAvailableVocabForLesson(@PathVariable Integer lessonId) {
        List<Vocabulary> availableVocab = lessonRepository.findAvailableVocabForLesson(lessonId);
        return ResponseEntity.ok(availableVocab);
    }

    @PostMapping("/lesson-vocab/batch-add")
    @Transactional
    public ResponseEntity<?> batchAddVocabToLesson(@RequestBody List<Map<String, Object>> payload) {
        try {
            // payload: [{ lessonId, vocabId }, ...]
            // Lấy max displayOrder hiện tại một lần duy nhất
            if (payload.isEmpty()) return ResponseEntity.badRequest().body("Danh sách rỗng!");

            Integer lessonId = Integer.parseInt(payload.get(0).get("lessonId").toString());
            Integer currentMax = lessonRepository.getMaxDisplayOrder(lessonId);
            int order = (currentMax != null ? currentMax : 0);

            int added = 0;
            for (Map<String, Object> item : payload) {
                Integer vocabId = Integer.parseInt(item.get("vocabId").toString());
                if (!lessonRepository.isVocabInLesson(lessonId, vocabId)) {
                    order++;
                    lessonRepository.addVocabToLesson(lessonId, vocabId, order);
                    added++;
                }
            }
            return ResponseEntity.ok("Đã thêm " + added + " từ vựng vào bài học");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }

    // Xóa từ vựng khỏi bài học
    @DeleteMapping("/lesson-vocab")
    @Transactional
    public ResponseEntity<?> removeVocabFromLesson(
            @RequestParam Integer lessonId,
            @RequestParam Integer vocabId) {
        try {
            int deleted = lessonRepository.removeVocabFromLesson(lessonId, vocabId);
            if (deleted > 0) {
                return ResponseEntity.ok("Đã xóa từ vựng khỏi bài học");
            } else {
                return ResponseEntity.badRequest().body("Không tìm thấy từ vựng trong bài học này!");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }

    // Kiểm tra từ vựng có đang được dùng trong InteractivePoint của lesson không
    @GetMapping("/vocab/{vocabId}/check-usage-in-lesson")
    public ResponseEntity<?> checkVocabUsageInLesson(
            @PathVariable Integer vocabId,
            @RequestParam Integer lessonId) {
        try {
            // Đếm số interactive points có sử dụng từ vựng này trong các scene của lesson
            int pointCount = lessonRepository.countInteractivePointsByVocabAndLesson(vocabId, lessonId);

            Map<String, Object> result = new HashMap<>();
            result.put("hasInteractivePoints", pointCount > 0);
            result.put("pointCount", pointCount);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }

    @DeleteMapping("/{lessonId}/vocab/all")
    @Transactional
    public ResponseEntity<?> removeAllVocabFromLesson(@PathVariable Integer lessonId) {
        try {
            // Kiểm tra xem có từ vựng nào đang được dùng trong InteractivePoint không
            int usageCount = lessonRepository.countInteractivePointsByLesson(lessonId);

            if (usageCount > 0) {
                return ResponseEntity.badRequest()
                        .body("Không thể xóa: Có " + usageCount + " điểm tương tác đang sử dụng từ vựng trong bài học này!");
            }

            int deletedCount = lessonRepository.removeAllVocabFromLesson(lessonId);
            return ResponseEntity.ok("Đã xóa " + deletedCount + " từ vựng khỏi bài học");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }


    // Helper: Đồng bộ total_lessons cho một category
    private void syncTotalLessons(Integer categoryId) {
        if (categoryId == null) return;
        categoryRepository.findById(categoryId).ifPresent(cat -> {
            int count = lessonRepository.countActiveByCategory(categoryId);
            cat.setTotalLessons(count);
            categoryRepository.save(cat);
        });
    }


    // 6. API lấy danh sách lesson theo category (chỉ id + name, chỉ active) - Dùng cho dropdown thêm từ vựng
    @GetMapping("/active/by-category/{catId}")
    public ResponseEntity<List<Map<String, Object>>> getActiveLessonsByCategory(@PathVariable Integer catId) {
        List<Lessons> lessons = lessonRepository.findByCategoryIdAndIsActiveTrue(catId);

        List<Map<String, Object>> result = lessons.stream().map(lesson -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", lesson.getId());
            item.put("lessonName", lesson.getLessonName());
            item.put("orderIndex", lesson.getOrderIndex());
            return item;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }
}