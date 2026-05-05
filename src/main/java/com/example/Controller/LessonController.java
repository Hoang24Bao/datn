package com.example.Controller;

import com.example.Dto.Response.LessonDTO;
import com.example.Entity.Categories;
import com.example.Entity.Lessons;
import com.example.Repository.CategoriesRepository;
import com.example.Repository.LessonsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/study")
public class LessonController {

    @Autowired
    private LessonsRepository lessonRepository;
    @Autowired
    private CategoriesRepository categoriesRepository;

    @GetMapping("/lessons")
    public String getLessons(@RequestParam("id") Integer categoryId, Model model) {
        // Dùng native query để lấy dữ liệu
        List<Object[]> rows = lessonRepository.findActiveLessonsDTO(categoryId);

        List<LessonDTO> lessons = rows.stream()
                .map(row -> new LessonDTO(
                        (Integer) row[0],
                        (String) row[1],
                        (Integer) row[2],
                        (Boolean) row[3],
                        (Boolean) row[4]
                ))
                .collect(Collectors.toList());

        model.addAttribute("lessons", lessons);

        Categories categories = categoriesRepository.findById(categoryId).orElse(null);
        model.addAttribute("categories", categories);
        model.addAttribute("currentPage", "categories");

        return "study/lessons";
    }
}