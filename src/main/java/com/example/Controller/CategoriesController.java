package com.example.Controller;

import com.example.Entity.Categories;
import com.example.Repository.CategoriesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/study")
public class CategoriesController {

    @Autowired
    private CategoriesRepository categoryRepository;

    @GetMapping("/categories")
    public String listCategories(Model model) {
        model.addAttribute("categories", categoryRepository.findByIsActiveTrue());
        model.addAttribute("currentPage", "categories");
        return "study/categories";
    }

    @GetMapping("/api/admin/categories/active")
    @ResponseBody
    public List<Categories> getActiveCategories() {
        return categoryRepository.findByIsActiveTrue();
    }


}
