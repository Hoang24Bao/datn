package com.example.Controller;

import com.example.Entity.Categories;
import com.example.Repository.CategoriesRepository;
import com.example.Repository.LessonsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/study") // Thêm tiền tố để quản lý đường dẫn tốt hơn
public class LessonController {

    @Autowired
    private LessonsRepository lessonRepository;
    @Autowired
    private CategoriesRepository categoriesRepository;

    @GetMapping("/lessons")
    public String getLessons(@RequestParam("id") Integer categoryId, Model model) {
        // 1. Lấy danh sách bài học
        model.addAttribute("lessons", lessonRepository.findByCategoryId(categoryId));

        // 2. Sửa lỗi: Sử dụng biến 'categoryRepository' (viết thường chữ đầu)
        // thay vì 'CategoriesRepository' (tên Interface)
        Categories categories = categoriesRepository.findById(categoryId).orElse(null);
        model.addAttribute("categories", categories);

        model.addAttribute("currentPage", "categories");
        return "study/lessons";
    }

}