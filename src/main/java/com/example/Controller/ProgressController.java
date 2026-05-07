package com.example.Controller;

import com.example.Entity.Progress;
import com.example.Entity.Users;
import com.example.Repository.ProgressRepository;
import com.example.Repository.UsersRepository;
import com.example.Repository.VocabularyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/progress")
public class ProgressController {

    @Autowired
    private ProgressRepository progressRepository;

    @Autowired
    private UsersRepository usersRepository;

    @Autowired
    private VocabularyRepository vocabularyRepository;

    // Lấy current user
    private Users getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return null;
        }
        String username = auth.getName();
        Optional<Users> userOpt = usersRepository.findByUserName(username);
        return userOpt.orElse(null);
    }

    /**
     * API lấy progress batch cho nhiều vocab cùng lúc
     * GET /api/progress/batch?vocabIds=1,2,3,4&lessonId=5
     */
    @GetMapping("/batch")
    public ResponseEntity<?> getProgressBatch(
            @RequestParam String vocabIds,
            @RequestParam(required = false) Integer lessonId) {

        Users currentUser = getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Chưa đăng nhập"));
        }

        try {
            // Parse danh sách vocab IDs
            List<Integer> vocabIdList = Arrays.stream(vocabIds.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());

            if (vocabIdList.isEmpty()) {
                return ResponseEntity.ok(Collections.emptyList());
            }

            // Lấy progress từ database
            List<Progress> progressList = progressRepository.findByUserIdAndVocabIdIn(
                    currentUser.getId(), vocabIdList);

            // Chuyển đổi thành response DTO
            List<Map<String, Object>> response = progressList.stream().map(p -> {
                Map<String, Object> item = new HashMap<>();
                item.put("vocabId", p.getId().getVocabId());
                item.put("isLearned", p.getIsLearned());
                item.put("memoryLevel", p.getMemoryLevel());
                item.put("lastReviewed", p.getLastReviewed());
                return item;
            }).collect(Collectors.toList());

            return ResponseEntity.ok(response);

        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid vocabIds format"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * API cập nhật progress cho 1 từ vựng
     * POST /api/progress/update
     */
    @PostMapping("/update")
    public ResponseEntity<?> updateProgress(@RequestBody Map<String, Object> payload) {

        Users currentUser = getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Chưa đăng nhập"));
        }

        try {
            // Lấy dữ liệu từ request
            Integer vocabId = (Integer) payload.get("vocabId");
            Integer lessonId = (Integer) payload.get("lessonId");
            Boolean isLearned = (Boolean) payload.get("isLearned");
            Integer memoryLevel = (Integer) payload.get("memoryLevel");

            if (vocabId == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "vocabId is required"));
            }

            // Kiểm tra vocab có tồn tại không
            if (!vocabularyRepository.existsById(vocabId)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Vocabulary not found"));
            }

            // Tìm hoặc tạo mới Progress
            Progress.ProgressId progressId = new Progress.ProgressId(currentUser.getId(), vocabId);
            Progress progress = progressRepository.findById(progressId).orElse(new Progress());

            if (progress.getId() == null) {
                // Tạo mới
                progress.setId(progressId);
                progress.setUser(currentUser);
                // Lấy vocabulary object (cần thiết cho mapping)
                vocabularyRepository.findById(vocabId).ifPresent(progress::setVocab);
            }

            // Cập nhật giá trị
            progress.setIsLearned(isLearned != null ? isLearned : false);
            progress.setMemoryLevel(memoryLevel != null ? memoryLevel : 1);
            progress.setLastReviewed(LocalDateTime.now());

            // Lưu vào database
            progressRepository.save(progress);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Đã cập nhật tiến trình học");
            response.put("vocabId", vocabId);
            response.put("isLearned", progress.getIsLearned());
            response.put("memoryLevel", progress.getMemoryLevel());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * API lấy progress cho 1 từ vựng cụ thể
     * GET /api/progress/{vocabId}
     */
    @GetMapping("/{vocabId}")
    public ResponseEntity<?> getProgressByVocabId(@PathVariable Integer vocabId) {

        Users currentUser = getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Chưa đăng nhập"));
        }

        try {
            Progress.ProgressId progressId = new Progress.ProgressId(currentUser.getId(), vocabId);
            Optional<Progress> progressOpt = progressRepository.findById(progressId);

            Map<String, Object> response = new HashMap<>();
            response.put("vocabId", vocabId);

            if (progressOpt.isPresent()) {
                Progress p = progressOpt.get();
                response.put("isLearned", p.getIsLearned());
                response.put("memoryLevel", p.getMemoryLevel());
                response.put("lastReviewed", p.getLastReviewed());
            } else {
                response.put("isLearned", false);
                response.put("memoryLevel", 1);
                response.put("lastReviewed", null);
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}