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
}