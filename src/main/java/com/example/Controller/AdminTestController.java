package com.example.Controller;

import com.example.Dto.Request.CreateTestDTO;
import com.example.Dto.Response.TestResponseDTO;
import com.example.Entity.Categories;
import com.example.Entity.TestQuestions;
import com.example.Entity.Tests;
import com.example.Repository.CategoriesRepository;
import com.example.Repository.TestQuestionsRepository;
import com.example.Repository.TestRepository;
import com.example.Service.TestService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/tests")
public class AdminTestController {

    @Autowired
    private TestService testService;

    @Autowired
    private TestRepository testRepository;

    @Autowired
    private CategoriesRepository categoriesRepository;

    @Autowired
    private TestQuestionsRepository testQuestionRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Lấy danh sách tất cả test
    @GetMapping("/all")
    public ResponseEntity<?> getAllTests() {
        List<Tests> tests = testRepository.findAll();
        List<Map<String, Object>> result = tests.stream().map(test -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", test.getId());
            map.put("title", test.getTitle());
            map.put("categoryId", test.getCategoryId());

            String categoryName = categoriesRepository.findById(test.getCategoryId())
                    .map(Categories::getCategoryName)
                    .orElse("Không xác định");
            map.put("categoryName", categoryName);

            map.put("durationMinutes", test.getDurationMinutes());
            map.put("maxScore", test.getMaxScore());
            map.put("passScore", test.getPassScore());
            map.put("questionCount", test.getQuestionCount());
            map.put("isActive", test.getIsActive());
            map.put("createdAt", test.getCreatedAt());
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    // Tạo test mới
    @PostMapping("/create")
    public ResponseEntity<?> createTest(@RequestBody CreateTestDTO dto) {
        try {
            TestResponseDTO test = testService.createTest(dto);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Tạo bài test thành công!",
                    "test", test
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        }
    }

    // Lấy chi tiết test
    @GetMapping("/{id}")
    public ResponseEntity<?> getTestDetail(@PathVariable Integer id) {
        try {
            Tests test = testRepository.findById(id).orElse(null);
            if (test == null) {
                return ResponseEntity.notFound().build();
            }

            List<TestQuestions> questions = testQuestionRepository.findByTestIdOrderByOrderIndexAsc(id);

            Map<String, Object> result = new HashMap<>();
            result.put("id", test.getId());
            result.put("title", test.getTitle());
            result.put("categoryId", test.getCategoryId());
            result.put("durationMinutes", test.getDurationMinutes());
            result.put("maxScore", test.getMaxScore());
            result.put("passScore", test.getPassScore());
            result.put("questionCount", test.getQuestionCount());
            result.put("isActive", test.getIsActive());

            List<Map<String, Object>> questionList = new ArrayList<>();
            for (TestQuestions q : questions) {
                Map<String, Object> qMap = new HashMap<>();
                qMap.put("id", q.getId());
                qMap.put("questionText", q.getQuestionText());
                qMap.put("options", objectMapper.readValue(q.getOptions(), List.class));
                qMap.put("correctAnswer", q.getCorrectAnswer());
                qMap.put("orderIndex", q.getOrderIndex());
                questionList.add(qMap);
            }
            result.put("questions", questionList);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Cập nhật test
    @PutMapping("/{id}")
    public ResponseEntity<?> updateTest(@PathVariable Integer id, @RequestBody Map<String, Object> payload) {
        try {
            Tests test = testRepository.findById(id).orElse(null);
            if (test == null) {
                return ResponseEntity.notFound().build();
            }

            if (payload.containsKey("title")) {
                test.setTitle((String) payload.get("title"));
            }
            if (payload.containsKey("durationMinutes")) {
                test.setDurationMinutes((Integer) payload.get("durationMinutes"));
            }
            if (payload.containsKey("maxScore")) {
                test.setMaxScore((Integer) payload.get("maxScore"));
            }
            if (payload.containsKey("passScore")) {
                test.setPassScore(((Number) payload.get("passScore")).floatValue());
            }
            if (payload.containsKey("isActive")) {
                test.setIsActive((Boolean) payload.get("isActive"));
            }

            testRepository.save(test);
            return ResponseEntity.ok(Map.of("success", true, "message", "Cập nhật thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    // Xóa test
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTest(@PathVariable Integer id) {
        try {
            testRepository.deleteById(id);
            return ResponseEntity.ok(Map.of("success", true, "message", "Xóa test thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Không thể xóa test này"));
        }
    }

    // Lấy danh sách category
    @GetMapping("/categories")
    public ResponseEntity<?> getCategories() {
        List<Categories> categories = categoriesRepository.findAll();
        List<Map<String, Object>> result = categories.stream()
                .filter(c -> c.getIsActive())
                .map(c -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", c.getId());
                    map.put("name", c.getCategoryName());
                    return map;
                }).collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/paging")
    public ResponseEntity<?> getTestsPaging(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) Boolean isActive) {

        try {
            org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(
                    page, size, org.springframework.data.domain.Sort.by("id").descending());

            org.springframework.data.domain.Page<Tests> testsPage;

            if (search != null && !search.isEmpty()) {
                testsPage = testRepository.findByTitleContainingIgnoreCaseAndFilters(search, categoryId, isActive, pageable);
            } else {
                testsPage = testRepository.findByFilters(categoryId, isActive, pageable);
            }

            List<Map<String, Object>> result = testsPage.getContent().stream().map(test -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", test.getId());
                map.put("title", test.getTitle());
                map.put("categoryId", test.getCategoryId());

                String categoryName = categoriesRepository.findById(test.getCategoryId())
                        .map(Categories::getCategoryName)
                        .orElse("Không xác định");
                map.put("categoryName", categoryName);

                map.put("durationMinutes", test.getDurationMinutes());
                map.put("maxScore", test.getMaxScore());
                map.put("passScore", test.getPassScore());
                map.put("questionCount", test.getQuestionCount());
                map.put("isActive", test.getIsActive());
                map.put("createdAt", test.getCreatedAt());
                return map;
            }).collect(Collectors.toList());

            return ResponseEntity.ok(Map.of(
                    "content", result,
                    "totalPages", testsPage.getTotalPages(),
                    "totalElements", testsPage.getTotalElements(),
                    "number", testsPage.getNumber(),
                    "size", testsPage.getSize()
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}