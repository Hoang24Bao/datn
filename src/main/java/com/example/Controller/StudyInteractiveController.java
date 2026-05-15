package com.example.Controller;

import com.example.Entity.InteractiveScene;
import com.example.Entity.Categories;
import com.example.Repository.InteractivePointRepository;
import com.example.Repository.InteractiveSceneRepository;
import com.example.Repository.CategoriesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/study")
public class StudyInteractiveController {

    @Autowired
    private InteractiveSceneRepository sceneRepository;

    @Autowired
    private InteractivePointRepository pointRepository;

    @Autowired
    private CategoriesRepository categoriesRepository;  // Đổi từ LessonsRepository

    @GetMapping("/interactive")
    public String interactiveStudy(@RequestParam("categoryId") Integer categoryId, Model model) {
        // Lấy TẤT CẢ scenes của category theo categoryId
        List<InteractiveScene> scenes = sceneRepository.findByCategoryIdOrderByOrderIndexAsc(categoryId);

        if (scenes.isEmpty()) {
            // Lấy category để có thông tin
            categoriesRepository.findById(categoryId).ifPresent(category -> {
                model.addAttribute("currentCateId", category.getId());
                model.addAttribute("category", category);
            });

            model.addAttribute("hasScenes", false);
            model.addAttribute("message", "Chủ đề này chưa có hình ảnh tương tác nào. Vui lòng quay lại sau!");
            model.addAttribute("scenes", new ArrayList<>());
            return "study/interactive";
        }

        // Load points cho từng scene
        scenes.forEach(scene -> {
            var points = pointRepository.findBySceneId(scene.getId());
            points.forEach(point -> {
                point.getVocabulary().getExpression(); // Trigger lazy loading
            });
            scene.setPoints(points);
        });

        model.addAttribute("hasScenes", true);
        model.addAttribute("scenes", scenes);
        model.addAttribute("currentCateId", scenes.get(0).getCategoryId());
        model.addAttribute("category", scenes.get(0).getCategory());

        return "study/interactive";
    }
}