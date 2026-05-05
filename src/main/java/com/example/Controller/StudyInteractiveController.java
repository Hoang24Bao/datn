package com.example.Controller;

import com.example.Entity.InteractiveScene;
import com.example.Repository.InteractivePointRepository;
import com.example.Repository.InteractiveSceneRepository;
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

    @GetMapping("/interactive")
    public String interactiveStudy(@RequestParam("lessonId") Integer lessonId, Model model) {
        // Lấy TẤT CẢ scenes của bài học theo lessonId
        List<InteractiveScene> scenes = sceneRepository.findByLessonIdOrderByOrderIndexAsc(lessonId);

        // ✅ Nếu chưa có scene nào, hiển thị thông báo thay vì lỗi
        if (scenes.isEmpty()) {
            model.addAttribute("hasScenes", false);
            model.addAttribute("message", "Bài học này chưa có hình ảnh tương tác nào. Vui lòng quay lại sau!");
            model.addAttribute("scenes", new ArrayList<>());
            model.addAttribute("currentCateId", null);
            model.addAttribute("lesson", null);
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
        model.addAttribute("currentCateId", scenes.get(0).getLesson().getCategoryId());
        model.addAttribute("lesson", scenes.get(0).getLesson());

        return "study/interactive";
    }
}