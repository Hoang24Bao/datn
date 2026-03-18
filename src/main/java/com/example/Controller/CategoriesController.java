package com.example.Controller;

import com.example.Repository.CategoriesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/study")
public class CategoriesController {

    @Autowired
    private CategoriesRepository categoryRepository;

    @GetMapping("/categories")
    public String listCategories(Model model) {
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("currentPage", "categories");
        return "study/categories"; // Tìm file trong templates/study/categories.html
    }
}
