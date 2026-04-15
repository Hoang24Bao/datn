package com.example.Controller;

import com.example.Entity.Vocabulary;
import com.example.Repository.VocabularyRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/vocab")
public class AdminVocabController {

    @Autowired
    private VocabularyRepository vocabularyRepository;

    @GetMapping("/all")
    public ResponseEntity<?> getAllVocab(
            @RequestParam(required = false) String level,
            @RequestParam(required = false) Integer cateId,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        // Nếu JS gửi chuỗi rỗng, hãy coi nó là null
        String cleanLevel = (level != null && level.isEmpty()) ? null : level;
        String cleanSearch = (search != null && search.isEmpty()) ? null : search;

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        Page<Vocabulary> result = vocabularyRepository.findByFilters(cleanLevel, cateId, cleanSearch, pageable);

        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteVocab(@PathVariable Integer id) {
        if (!vocabularyRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        vocabularyRepository.deleteById(id);
        return ResponseEntity.ok("Xóa từ vựng thành công!");
    }


    @PostMapping("/add")
    @Transactional // Cực kỳ quan trọng vì lưu 2 bảng
    public ResponseEntity<?> addVocab(@RequestBody Map<String, Object> payload) {
        try {
            // 1. Tạo đối tượng Vocabulary
            Vocabulary v = new Vocabulary();
            v.setExpression((String) payload.get("expression"));
            v.setKana((String) payload.get("kana"));
            v.setRomaji((String) payload.get("romaji"));
            v.setMeaning((String) payload.get("meaning"));
            v.setWordType((String) payload.get("wordType"));
            v.setExample((String) payload.get("example"));
            v.setExampleVi((String) payload.get("exampleVi"));
            v.setImageUrl((String) payload.get("imageUrl"));
            v.setAudioUrl((String) payload.get("audioUrl"));

            // Lưu vào bảng Vocabulary trước để có ID
            Vocabulary savedVocab = vocabularyRepository.save(v);

            // 2. Lưu vào bảng trung gian Lesson_Vocab (Native Query cho nhanh)
            Integer lessonId = Integer.parseInt(payload.get("lessonId").toString());

            // Bạn cần viết thêm hàm insert này trong Repository
            vocabularyRepository.insertLessonVocab(lessonId, savedVocab.getId());

            return ResponseEntity.ok("Thêm thành công");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }
}