package com.example.Controller;

import com.example.Entity.InteractiveScene;
import com.example.Repository.InteractiveSceneRepository;
import com.example.Repository.LessonsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class InteractiveController {

    @Autowired
    private InteractiveSceneRepository sceneRepository;

    @Autowired
    private LessonsRepository lessonRepository;

    @GetMapping("/study/interactive")
    public String viewInteractive(@RequestParam("id") Integer lessonId, Model model) {
        // 1. Lấy thông tin bài học
        var lesson = lessonRepository.findById(lessonId).orElse(null);

        // 2. Lấy danh sách các cảnh (Scenes) của bài học đó
        List<InteractiveScene> scenes = sceneRepository.findByLessonIdOrderByOrderIndexAsc(lessonId);

        // 3. Đưa dữ liệu sang giao diện Thymeleaf
        model.addAttribute("lesson", lesson);
        model.addAttribute("scenes", scenes);
        model.addAttribute("currentCateId", lesson != null ? lesson.getCategory().getId() : null);

        return "study/interactive"; // Trả về file interactive.html
    }
}