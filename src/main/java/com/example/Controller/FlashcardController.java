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
public class FlashcardController {

    @Autowired
    private VocabularyRepository vocabularyRepository;

    @Autowired
    private LessonsRepository lessonRepository;

    @GetMapping("/flashcards")
    public String showFlashcards(@RequestParam("id") Integer lessonId, Model model) {
        Lessons lesson = lessonRepository.findById(lessonId).orElse(null);
        List<Vocabulary> vocabs = vocabularyRepository.findByLessonId(lessonId);

        model.addAttribute("lesson", lesson);
        model.addAttribute("vocabs", vocabs);
        model.addAttribute("categoryId", lesson.getCategoryId());

        if (lesson != null) {
            model.addAttribute("currentCateId", lesson.getCategoryId());
        }

        return "study/flashcards";
    }
}