package com.example.Controller;

import com.example.Repository.VocabularyRepository;
import com.example.Repository.LessonsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/study")
public class VocabularyController {

    @Autowired
    private VocabularyRepository vocabularyRepository;

    @Autowired
    private LessonsRepository lessonsRepository;

    //Lấy danh sách từ vựng theo bài học
    @GetMapping("/lesson/{lessonId}/vocab")
    public String listVocabByLesson(@PathVariable Integer lessonId, Model model) {
        model.addAttribute("vocabList", vocabularyRepository.findByLessonId(lessonId));
        model.addAttribute("lesson", lessonsRepository.findById(lessonId).orElse(null));
        model.addAttribute("currentPage", "vocabulary");
        return "study/vocabulary";
    }
}