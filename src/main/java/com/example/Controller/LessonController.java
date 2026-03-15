package com.example.Controller;

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
    private CategoriesRepository categoryRepository;

    @GetMapping("/lesson")
    public String getLessons(@RequestParam("id") Integer categoryId, Model model) {
        // Logic lấy bài học và banner thumbnail-x.jpg đã nói ở trên
        model.addAttribute("currentPage", "categories");
        return "lesson";
    }

    @GetMapping("/flashcards")
    public String getFlashcards(@RequestParam("id") Long id, Model model) {
        model.addAttribute("lessonId", id);
        model.addAttribute("currentPage", "categories");
        return "flashcards";
    }
}