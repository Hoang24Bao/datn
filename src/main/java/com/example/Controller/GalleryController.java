package com.example.Controller;

import com.example.Entity.Lessons;
import com.example.Entity.Vocabulary;
import com.example.Repository.LessonsRepository;
import com.example.Repository.VocabularyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/study")
public class GalleryController {

    @Autowired
    private VocabularyRepository vocabularyRepository;

    @Autowired
    private LessonsRepository lessonRepository;

    @GetMapping("/gallery")
    public String showGallery(@RequestParam("id") Integer lessonId, Model model) {
        // 1. Lấy thông tin bài học
        Lessons lesson = lessonRepository.findById(lessonId).orElse(null);

        // 2. Lấy danh sách từ vựng của bài đó
        List<Vocabulary> vocabs = vocabularyRepository.findByLessonId(lessonId);

        // 3. Đẩy dữ liệu sang HTML
        model.addAttribute("lesson", lesson);
        model.addAttribute("vocabs", vocabs);

        // 4. Quan trọng: Lấy categoryId để nút "Quay lại" hoạt động đúng
        if (lesson != null) {
            model.addAttribute("currentCateId", lesson.getCategoryId());
        }

        return "study/gallery"; // Trỏ tới templates/study/gallery.html
    }
}
