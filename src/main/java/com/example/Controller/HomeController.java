package com.example.Controller;

import com.example.Entity.Categories;
import com.example.Entity.Users;
import com.example.Repository.CategoriesRepository;
import com.example.Repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

@Controller
public class HomeController {

    @Autowired
    private CategoriesRepository categoriesRepository;

    @Autowired
    private UsersRepository usersRepository;

    @GetMapping({"/", "/home"})
    public String home(Model model) {
        model.addAttribute("currentPage", "home");
        List<Categories> activeCategories = categoriesRepository.findByIsActiveTrue();
        model.addAttribute("categories", activeCategories);
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


    @GetMapping("/profile")
    public String profile(Model model) {
        model.addAttribute("currentPage", "profile");

        // Lấy thông tin user hiện tại
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            String username = auth.getName();
            Optional<Users> userOpt = usersRepository.findByUserName(username);
            if (userOpt.isPresent()) {
                Users user = userOpt.get();
                model.addAttribute("user", user);
                model.addAttribute("levelName", getLevelName(user.getLevelId()));
                model.addAttribute("lessonCount", 0);
                model.addAttribute("vocabCount", 0);
            }
        }

        return "profile";
    }

    private String getLevelName(Integer levelId) {
        if (levelId == null) return "N5";
        switch (levelId) {
            case 1:
                return "N5";
            case 2:
                return "N4";
            case 3:
                return "N3";
            case 4:
                return "N2";
            case 5:
                return "N1";
            default:
                return "N5";
        }
    }

    @GetMapping("/study/kanji")
    public String studyKanji(Model model) {
        model.addAttribute("currentPage", "kanji");
        return "study/kanji";  // Trả về view
    }

}