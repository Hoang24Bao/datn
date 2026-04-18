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

    // 1. Lấy danh sách từ vựng theo bài học (chỉ lấy active = true)
    @GetMapping("/lesson/{lessonId}/vocab")
    public String listVocabByLesson(@PathVariable Integer lessonId, Model model) {
        // Chỉ lấy từ vựng có is_active = true
        model.addAttribute("vocabList", vocabularyRepository.findByLessonId(lessonId));
        model.addAttribute("lesson", lessonsRepository.findById(lessonId).orElse(null));
        model.addAttribute("currentPage", "vocabulary");
        return "study/vocabulary";
    }

//    // 2. Lấy danh sách từ vựng theo category (tất cả lesson trong category)
//    @GetMapping("/category/{categoryId}/vocab")
//    public String listVocabByCategory(@PathVariable Integer categoryId, Model model) {
//        model.addAttribute("vocabList", vocabularyRepository.findByCategoryIdAndIsActiveTrue(categoryId));
//        model.addAttribute("categoryId", categoryId);
//        model.addAttribute("currentPage", "vocabulary");
//        return "study/vocabulary";
//    }

    // 3. Tìm kiếm từ vựng (chỉ active)
//    @GetMapping("/vocab/search")
//    public String searchVocab(@RequestParam String keyword, Model model) {
//        model.addAttribute("vocabList", vocabularyRepository.searchByKeywordAndIsActiveTrue(keyword));
//        model.addAttribute("keyword", keyword);
//        model.addAttribute("currentPage", "vocabulary");
//        return "study/vocabulary";
//    }
}