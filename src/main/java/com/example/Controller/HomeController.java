package com.example.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model; // Nhớ import thư viện này
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {

    @GetMapping({"/", "/home"})
    public String home(Model model) {
        model.addAttribute("currentPage", "home");
        return "index";
    }

    @GetMapping("/signup")
    public String signupPage() {
        return "signup"; // Phải khớp với tên file signup.html trong templates
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login"; // Phải khớp với tên file login.html
    }

    @GetMapping("/about")
    public String about(Model model) {
        model.addAttribute("currentPage", "about");
        return "about";
    }

    @GetMapping("/admin")
    public String adminPage() {
        return "admin";
    }

    @GetMapping("/admin/users")
    public String adminUsers() {
        return "admin-users";
    }

    @GetMapping("/admin/vocabulary")
    public String adminVocabulary() {
        return "admin-vocabulary";
    }
}