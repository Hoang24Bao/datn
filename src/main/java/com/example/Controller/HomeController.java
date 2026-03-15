package com.example.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model; // Nhớ import thư viện này
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {

    @GetMapping({"/","/home"})
    public String home(Model model) {
        model.addAttribute("currentPage", "home");
        return "index";
    }

    @GetMapping("/about")
    public String about(Model model) {
        model.addAttribute("currentPage", "about");
        return "about";
    }
//
//    @GetMapping("/categories")
//    public String categories(Model model) {
//        model.addAttribute("currentPage", "categories");
//        return "categories";
//    }
//
//    @GetMapping("/lesson")
//    public String lesson(Model model) {
//        // Đặt currentPage là 'categories' để menu "Học tập -> Từ vựng" vẫn giữ trạng thái active
//        model.addAttribute("currentPage", "categories");
//        return "lesson";
//    }
//
//    @GetMapping("/flashcards")
//    public String flashcards(@RequestParam("id") Long id, Model model) {
//        model.addAttribute("lessonId", id); // Gửi ID qua trang để biết đang học bài nào
//        model.addAttribute("currentPage", "categories");
//        return "flashcards"; // Tên file flashcards.html trong templates
//    }
}