package com.example.Controller;

import com.example.Entity.Tests;
import com.example.Entity.UserTestResults;
import com.example.Entity.Users;
import com.example.Repository.CategoriesRepository;
import com.example.Repository.TestRepository;
import com.example.Repository.UserTestResultRepository;
import com.example.Repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;
import java.util.stream.Collectors;

@Controller
public class HistoryController {

    @Autowired
    private UserTestResultRepository userTestResultRepository;

    @Autowired
    private TestRepository testsRepository;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private CategoriesRepository categoriesRepository;

    // Method cho đường dẫn /history (từ dropdown menu)
    @GetMapping("/history")
    public String history(Authentication authentication, Model model) {
        return showTestHistory(authentication, model, "all");
    }

    // Method chính cho /study/test-history
    @GetMapping("/study/test-history")
    public String showTestHistory(Authentication authentication, Model model,
                                  @RequestParam(defaultValue = "all") String filter) {

        Integer userId = getCurrentUserId(authentication);

        if (userId == null) {
            return "redirect:/login";
        }

        // Lấy danh sách kết quả test đã JOIN với Tests và Categories
        List<Object[]> results = userTestResultRepository.findHistoryWithDetails(userId);

        // Xây dựng danh sách hiển thị
        List<Map<String, Object>> historyList = results.stream().map(row -> {
            Map<String, Object> item = new HashMap<>();

            item.put("id", row[0]);
            item.put("testId", row[1]);
            item.put("testTitle", row[2]);
            item.put("categoryName", row[3]);
            item.put("questionCount", row[4]);

            Integer seconds = (Integer) row[5];
            String formattedDuration = (seconds != null && seconds > 0)
                    ? String.format("%02d:%02d", seconds / 60, seconds % 60)
                    : "Chưa hoàn thành";
            item.put("durationFormatted", formattedDuration);

            item.put("score", row[6]);
            item.put("maxScore", row[7]);
            item.put("completedAt", row[8]);
            item.put("isPassed", row[9]);

            return item;
        }).collect(Collectors.toList());

        // Lọc theo filter
        if ("passed".equals(filter)) {
            historyList = historyList.stream()
                    .filter(h -> (Boolean) h.get("isPassed"))
                    .collect(Collectors.toList());
        } else if ("failed".equals(filter)) {
            historyList = historyList.stream()
                    .filter(h -> !(Boolean) h.get("isPassed"))
                    .collect(Collectors.toList());
        }

        // Thống kê
        long totalTests = historyList.size();
        long passedTests = historyList.stream()
                .filter(h -> (Boolean) h.get("isPassed"))
                .count();
        double avgScore = historyList.stream()
                .mapToDouble(h -> ((Number) h.get("score")).doubleValue())
                .average()
                .orElse(0);

        model.addAttribute("historyList", historyList);
        model.addAttribute("totalTests", totalTests);
        model.addAttribute("passedTests", passedTests);
        model.addAttribute("avgScore", Math.round(avgScore));
        model.addAttribute("currentFilter", filter);

        return "test-history";
    }

    private Integer getCurrentUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        String username = authentication.getName();
        return usersRepository.findByUserName(username)
                .map(Users::getId)
                .orElse(null);
    }

    @GetMapping("/api/user/test-history")
    @ResponseBody
    public ResponseEntity<?> getTestHistory(@RequestParam(defaultValue = "all") String filter, Authentication authentication) {
        Integer userId = getCurrentUserId(authentication);
        if (userId == null) return ResponseEntity.status(401).build();

        // Lấy danh sách kết quả test đã JOIN với Tests và Categories
        List<Object[]> results = userTestResultRepository.findHistoryWithDetails(userId);

        // Xây dựng danh sách hiển thị
        List<Map<String, Object>> historyList = results.stream().map(row -> {
            Map<String, Object> item = new HashMap<>();

            item.put("id", row[0]);
            item.put("testId", row[1]);
            item.put("testTitle", row[2]);
            item.put("categoryName", row[3]);
            item.put("questionCount", row[4]);

            Integer seconds = (Integer) row[5];
            String formattedDuration = (seconds != null && seconds > 0)
                    ? String.format("%02d:%02d", seconds / 60, seconds % 60)
                    : "Chưa hoàn thành";
            item.put("durationFormatted", formattedDuration);

            item.put("score", row[6]);
            item.put("maxScore", row[7]);
            item.put("completedAt", row[8]);
            item.put("isPassed", row[9]);

            return item;
        }).collect(Collectors.toList());

        // Lọc theo filter
        if ("passed".equals(filter)) {
            historyList = historyList.stream()
                    .filter(h -> (Boolean) h.get("isPassed"))
                    .collect(Collectors.toList());
        } else if ("failed".equals(filter)) {
            historyList = historyList.stream()
                    .filter(h -> !(Boolean) h.get("isPassed"))
                    .collect(Collectors.toList());
        }

        // Thống kê
        long totalTests = historyList.size();
        long passedTests = historyList.stream()
                .filter(h -> (Boolean) h.get("isPassed"))
                .count();
        double avgScore = historyList.stream()
                .mapToDouble(h -> ((Number) h.get("score")).doubleValue())
                .average()
                .orElse(0);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalTests", totalTests);
        stats.put("passedTests", passedTests);
        stats.put("avgScore", avgScore);

        Map<String, Object> response = new HashMap<>();
        response.put("historyList", historyList);
        response.put("stats", stats);

        return ResponseEntity.ok(response);
    }
}