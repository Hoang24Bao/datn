package com.example.Controller;

import com.example.Entity.Vocabulary;
import com.example.Repository.VocabularyRepository;
import com.example.Service.AudioGenerationService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.example.Repository.InteractivePointRepository;

@RestController
@RequestMapping("/api/admin/vocab")
public class AdminVocabController {

    @Autowired
    private VocabularyRepository vocabularyRepository;
    @Autowired
    private AudioGenerationService audioGenerationService;

    @Autowired
    private InteractivePointRepository interactivePointRepository;

    @PostMapping("/generate-audio")
    public ResponseEntity<?> generateAudio(@RequestBody Map<String, String> request) {
        try {
            String romaji = request.get("romaji");
            String vocabIdStr = request.get("vocabId");

            Integer vocabId = (vocabIdStr != null && !vocabIdStr.isEmpty()) ?
                    Integer.parseInt(vocabIdStr) : 0;

            if (romaji == null || romaji.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Vui lòng nhập Romaji trước khi sinh audio!"
                ));
            }

            String audioUrl = audioGenerationService.generateAudio(romaji, vocabId);

            Map<String, String> response = new HashMap<>();
            response.put("audioUrl", audioUrl);
            response.put("message", "Đã sinh audio thành công!");

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "Không thể sinh audio: " + e.getMessage()
            ));
        }
    }

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

    @PatchMapping("/{id}/toggle-status")
    @Transactional
    public ResponseEntity<?> toggleVocabStatus(@PathVariable Integer id) {
        return vocabularyRepository.findById(id)
                .map(vocab -> {
                    boolean newStatus = !vocab.getIsActive();
                    vocab.setIsActive(newStatus);
                    vocabularyRepository.save(vocab);

                    String message = newStatus ? "Đã khôi phục từ vựng" : "Đã ẩn từ vựng";
                    return ResponseEntity.ok().body(message);
                })
                .orElse(ResponseEntity.notFound().build());
    }


    @PostMapping("/add")
    @Transactional
    public ResponseEntity<?> addVocab(@RequestBody Map<String, Object> payload) {
        try {
            // Kiểm tra trùng expression (tuỳ chọn)
            String expression = (String) payload.get("expression");
            if (vocabularyRepository.existsByExpression(expression)) {
                return ResponseEntity.badRequest().body("Từ vựng này đã tồn tại!");
            }

            // 1. Tạo đối tượng Vocabulary
            Vocabulary v = new Vocabulary();
            v.setExpression(expression);
            v.setKana((String) payload.get("kana"));
            v.setRomaji((String) payload.get("romaji"));
            v.setMeaning((String) payload.get("meaning"));
            v.setWordType((String) payload.get("wordType"));
            v.setExample((String) payload.get("example"));
            v.setExampleVi((String) payload.get("exampleVi"));
            v.setImageUrl((String) payload.get("imageUrl"));
            v.setAudioUrl((String) payload.get("audioUrl"));
            v.setIsActive(true);

            // Lưu vào bảng Vocabulary trước để có ID
            Vocabulary savedVocab = vocabularyRepository.save(v);


            // ← CHỈ gán vào bài học nếu lessonId được truyền lên
            Object lessonIdRaw = payload.get("lessonId");
            if (lessonIdRaw != null && !lessonIdRaw.toString().isBlank()
                    && !lessonIdRaw.toString().equals("0")) {
                Integer lessonId = Integer.parseInt(lessonIdRaw.toString());
                vocabularyRepository.insertLessonVocab(lessonId, savedVocab.getId());
            }

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Thêm từ vựng thành công!");
            response.put("id", savedVocab.getId());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }


    @GetMapping("/by-lesson/{lessonId}")
    public ResponseEntity<List<Map<String, Object>>> getVocabByLesson(@PathVariable Integer lessonId) {
        List<Vocabulary> vocabList = vocabularyRepository.findByLessonId(lessonId);
        List<Map<String, Object>> response = vocabList.stream().map(vocab -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", vocab.getId());
            item.put("expression", vocab.getExpression());
            item.put("kana", vocab.getKana());
            item.put("romaji", vocab.getRomaji());
            item.put("meaning", vocab.getMeaning());
            item.put("isActive", vocab.getIsActive());
            return item;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }


    @GetMapping("/{id}")
    public ResponseEntity<?> getVocabById(@PathVariable Integer id) {
        try {
            return vocabularyRepository.findById(id)
                    .map(vocab -> {
                        Map<String, Object> response = new HashMap<>();
                        response.put("id", vocab.getId());
                        response.put("expression", vocab.getExpression());
                        response.put("kana", vocab.getKana());
                        response.put("romaji", vocab.getRomaji());
                        response.put("meaning", vocab.getMeaning());
                        response.put("wordType", vocab.getWordType());
                        response.put("example", vocab.getExample());
                        response.put("exampleVi", vocab.getExampleVi());
                        response.put("imageUrl", vocab.getImageUrl());
                        response.put("audioUrl", vocab.getAudioUrl());
                        response.put("isActive", vocab.getIsActive());
                        return ResponseEntity.ok(response);
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }


    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<?> updateVocab(@PathVariable Integer id, @RequestBody Map<String, Object> payload) {
        try {
            // Tìm từ vựng cần cập nhật
            Vocabulary existingVocab = vocabularyRepository.findById(id).orElse(null);
            if (existingVocab == null) {
                return ResponseEntity.notFound().build();
            }

            // Cập nhật các trường
            String expression = (String) payload.get("expression");
            if (expression != null && !expression.trim().isEmpty()) {
                // Kiểm tra trùng tên (trừ chính nó)
                if (vocabularyRepository.existsByExpression(expression) &&
                        !expression.equals(existingVocab.getExpression())) {
                    return ResponseEntity.badRequest().body("Từ vựng này đã tồn tại!");
                }
                existingVocab.setExpression(expression);
            }

            if (payload.containsKey("kana")) {
                existingVocab.setKana((String) payload.get("kana"));
            }
            if (payload.containsKey("romaji")) {
                existingVocab.setRomaji((String) payload.get("romaji"));
            }
            if (payload.containsKey("meaning")) {
                existingVocab.setMeaning((String) payload.get("meaning"));
            }
            if (payload.containsKey("wordType")) {
                existingVocab.setWordType((String) payload.get("wordType"));
            }
            if (payload.containsKey("example")) {
                existingVocab.setExample((String) payload.get("example"));
            }
            if (payload.containsKey("exampleVi")) {
                existingVocab.setExampleVi((String) payload.get("exampleVi"));
            }
            if (payload.containsKey("imageUrl")) {
                existingVocab.setImageUrl((String) payload.get("imageUrl"));
            }
            if (payload.containsKey("audioUrl")) {
                existingVocab.setAudioUrl((String) payload.get("audioUrl"));
            }
            if (payload.containsKey("isActive")) {
                existingVocab.setIsActive((Boolean) payload.get("isActive"));
            }

            // Lưu lại
            vocabularyRepository.save(existingVocab);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Cập nhật từ vựng thành công!");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }


    /**
     * Kiểm tra từ vựng có đang được dùng trong InteractivePoint không
     */
    @GetMapping("/{id}/check-usage")
    public ResponseEntity<?> checkVocabUsage(@PathVariable Integer id) {
        return vocabularyRepository.findById(id)
                .map(vocab -> {
                    // Đếm số lượng interactive points dùng từ này
                    int pointCount = interactivePointRepository.countByVocabId(id);

                    Map<String, Object> response = new HashMap<>();
                    response.put("hasInteractivePoints", pointCount > 0);
                    response.put("pointCount", pointCount);
                    response.put("expression", vocab.getExpression());

                    if (pointCount > 0) {
                        // Lấy thông tin các scene để hiển thị
                        List<Map<String, Object>> scenes = interactivePointRepository.findSceneUsageByVocabId(id);
                        response.put("scenes", scenes);
                        if (!scenes.isEmpty()) {
                            response.put("firstLessonId", scenes.get(0).get("lessonId"));
                        }
                    }

                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Kiểm tra từ vựng có đang được dùng trong InteractivePoint của một bài học cụ thể không
     */
    @GetMapping("/{id}/check-usage-in-lesson")
    public ResponseEntity<?> checkVocabUsageInLesson(@PathVariable Integer id, @RequestParam Integer lessonId) {
        int pointCount = interactivePointRepository.countByVocabIdAndLessonId(id, lessonId);

        Map<String, Object> response = new HashMap<>();
        response.put("hasInteractivePoints", pointCount > 0);
        response.put("pointCount", pointCount);

        return ResponseEntity.ok(response);
    }
}