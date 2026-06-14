package com.example.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class TestPageController {

    @GetMapping("/test/tests")
    public String testsPage() {
        return "test/tests";
    }

    @GetMapping("/test/category-tests")
    public String categoryTestsPage(@RequestParam Integer categoryId, Model model) {
        model.addAttribute("categoryId", categoryId);
        return "test/category-tests";
    }

    // Trang làm bài test
    @GetMapping("/test/take")
    public String takeTestPage(@RequestParam Integer attemptId, Model model) {
        model.addAttribute("attemptId", attemptId);
        return "test/take-test";
    }
}