package com.example.Controller;

import com.example.Entity.Lessons;
import com.example.Entity.Vocabulary;
import com.example.Repository.LessonsRepository;
import com.example.Repository.VocabularyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/study")
public class MatchingController {

    @Autowired
    private LessonsRepository lessonRepository;

    @Autowired
    private VocabularyRepository vocabularyRepository;

    /**
     * Trang Nối nghĩa (View)
     * URL: /study/matching?id={lessonId}
     */
    @GetMapping("/matching")
    public String showMatchingPage(@RequestParam("id") Integer lessonId, Model model) {
        // Lấy thông tin lesson
        Lessons lesson = lessonRepository.findById(lessonId).orElse(null);

        if (lesson == null) {
            return "redirect:/study/categories";
        }

        model.addAttribute("lesson", lesson);
        model.addAttribute("currentCateId", lesson.getCategoryId());

        return "study/matching";
    }

    /**
     * API lấy từ vựng ngẫu nhiên cho trò chơi Nối nghĩa
     * URL: /study/api/matching/random?lessonId={lessonId}&limit=10
     */
    @GetMapping("/api/matching/random")
    @ResponseBody
    public ResponseEntity<List<VocabDTO>> getRandomVocabForMatching(
            @RequestParam("lessonId") Integer lessonId,
            @RequestParam(defaultValue = "10") int limit) {

        // Lấy tất cả từ vựng của lesson
        List<Vocabulary> allVocabs = vocabularyRepository.findByLessonId(lessonId);

        // Kiểm tra nếu không có dữ liệu
        if (allVocabs == null || allVocabs.isEmpty()) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        // Xáo trộn danh sách
        Collections.shuffle(allVocabs);

        // Lấy số lượng theo limit (nhưng không vượt quá tổng số từ)
        int actualLimit = Math.min(limit, allVocabs.size());
        List<Vocabulary> randomVocabs = allVocabs.stream()
                .limit(actualLimit)
                .collect(Collectors.toList());

        // Chuyển đổi sang DTO để trả về
        List<VocabDTO> result = randomVocabs.stream()
                .map(v -> new VocabDTO(v.getId(), v.getExpression(), v.getKana(), v.getMeaning()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    /**
     * DTO (Data Transfer Object) cho từ vựng
     * Chỉ chứa các trường cần thiết cho game
     */
    static class VocabDTO {
        private Integer id;
        private String expression;
        private String kana;
        private String meaning;

        public VocabDTO(Integer id, String expression, String kana, String meaning) {
            this.id = id;
            this.expression = expression;
            this.kana = kana;
            this.meaning = meaning;
        }

        // Getter và Setter (cần thiết để JSON serialize)
        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getExpression() {
            return expression;
        }

        public void setExpression(String expression) {
            this.expression = expression;
        }

        public String getKana() {
            return kana;
        }

        public void setKana(String kana) {
            this.kana = kana;
        }

        public String getMeaning() {
            return meaning;
        }

        public void setMeaning(String meaning) {
            this.meaning = meaning;
        }
    }
}