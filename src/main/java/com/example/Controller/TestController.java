package com.example.Controller;

import com.example.Dto.Request.CreateTestDTO;
import com.example.Dto.Response.TestResponseDTO;
import com.example.Dto.Response.TestResultDTO;
import com.example.Entity.*;
import com.example.Repository.*;
import com.example.Service.TestService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tests")
public class TestController {

    @Autowired
    private TestService testService;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private CategoriesRepository categoriesRepository;

    @Autowired
    private TestRepository testRepository;

    @Autowired
    private UserTestResultRepository userTestResultRepository;

    @Autowired
    private TestQuestionsRepository testQuestionRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private Users getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return usersRepository.findByUserName(username).orElse(null);
    }


    @PostMapping("/admin/create")
    public ResponseEntity<?> createTest(@RequestBody CreateTestDTO dto) {
        try {
            TestResponseDTO test = testService.createTest(dto);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Tạo bài test thành công",
                    "test", test
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        }
    }


    @GetMapping("/category/{categoryId}")
    public ResponseEntity<?> getTestsByCategory(@PathVariable Integer categoryId) {
        try {
            Users currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Chưa đăng nhập"));
            }

            List<TestResponseDTO> tests = testService.getTestsByCategory(categoryId, currentUser.getId());
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "tests", tests
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Bắt đầu làm test
    @PostMapping("/{testId}/start")
    public ResponseEntity<?> startTest(@PathVariable Integer testId) {
        try {
            Users currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Chưa đăng nhập"));
            }

            Map<String, Object> result = testService.startTest(testId, currentUser.getId());
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", result
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Không thể bắt đầu bài test"
            ));
        }
    }

    // Nộp bài
    @PostMapping("/attempt/{attemptId}/submit")
    public ResponseEntity<?> submitTest(
            @PathVariable Integer attemptId,
            @RequestBody Map<String, List<Integer>> payload) {
        try {
            Users currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Chưa đăng nhập"));
            }

            List<Integer> selectedAnswers = payload.get("selectedAnswers");
            TestResultDTO result = testService.submitTest(attemptId, selectedAnswers);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "result", result
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Không thể nộp bài"
            ));
        }
    }


    @GetMapping("/categories-with-status")
    public ResponseEntity<?> getTestCategoriesWithStatus() {
        try {
            Users currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Chưa đăng nhập"));
            }

            List<Categories> categories = categoriesRepository.findAll();

            List<Map<String, Object>> result = categories.stream()
                    .filter(Categories::getIsActive)
                    .map(c -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("id", c.getId());
                        map.put("name", c.getCategoryName());
                        map.put("iconUrl", c.getIconUrl());
                        map.put("thumbnailUrl", c.getThumbnailUrl());
                        map.put("jlptLevel", c.getJlptLevel());

                        boolean hasCompleted = userTestResultRepository.hasUserCompletedAnyTestInCategory(currentUser.getId(), c.getId());
                        map.put("isCompleted", hasCompleted);

                        int totalTests = testRepository.countByCategoryId(c.getId());
                        int completedTests = userTestResultRepository.countCompletedTestsByUserAndCategory(currentUser.getId(), c.getId());
                        int progressPercent = totalTests > 0 ? (completedTests * 100 / totalTests) : 0;
                        map.put("progressPercent", progressPercent);

                        return map;
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/attempt/{attemptId}/info")
    public ResponseEntity<?> getAttemptInfo(@PathVariable Integer attemptId) {
        try {
            UserTestResults attempt = userTestResultRepository.findById(attemptId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy attempt"));

            Tests test = testRepository.findById(attempt.getTestId()).orElseThrow();
            List<TestQuestions> questions = testQuestionRepository.findByTestIdOrderByOrderIndexAsc(test.getId());

            List<Map<String, Object>> questionList = new ArrayList<>();
            for (TestQuestions q : questions) {
                Map<String, Object> qMap = new HashMap<>();
                qMap.put("id", q.getId());
                qMap.put("questionText", q.getQuestionText());
                qMap.put("options", objectMapper.readValue(q.getOptions(), List.class));
                qMap.put("orderIndex", q.getOrderIndex());
                questionList.add(qMap);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("attemptId", attempt.getId());
            result.put("testId", test.getId());
            result.put("testTitle", test.getTitle());
            result.put("durationMinutes", test.getDurationMinutes());
            result.put("maxScore", test.getMaxScore());
            result.put("questionCount", questions.size());
            result.put("questions", questionList);

            return ResponseEntity.ok(Map.of("success", true, "data", result));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}