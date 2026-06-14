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
     * GET /api/progress/batch?vocabIds=1,2,3,4
     */
    @GetMapping("/batch")
    public ResponseEntity<?> getProgressBatch(@RequestParam String vocabIds) {

        Users currentUser = getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Chưa đăng nhập"));
        }

        try {
            List<Integer> vocabIdList = Arrays.stream(vocabIds.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());

            if (vocabIdList.isEmpty()) {
                return ResponseEntity.ok(Collections.emptyList());
            }

            List<Progress> progressList = progressRepository.findByUserIdAndVocabIdIn(
                    currentUser.getId(), vocabIdList);

            List<Map<String, Object>> response = progressList.stream().map(p -> {
                Map<String, Object> item = new HashMap<>();
                item.put("vocabId", p.getId().getVocabId());
                item.put("isLearned", p.getIsLearned());
                item.put("memoryLevel", p.getMemoryLevel());
                item.put("lastReviewed", p.getLastReviewed());
                return item;
            }).collect(Collectors.toList());

            // Thêm các vocab chưa có progress (mặc định memoryLevel = 1)
            Set<Integer> existingVocabIds = progressList.stream()
                    .map(p -> p.getId().getVocabId())
                    .collect(Collectors.toSet());

            for (Integer vocabId : vocabIdList) {
                if (!existingVocabIds.contains(vocabId)) {
                    Map<String, Object> defaultItem = new HashMap<>();
                    defaultItem.put("vocabId", vocabId);
                    defaultItem.put("isLearned", false);
                    defaultItem.put("memoryLevel", 1);
                    defaultItem.put("lastReviewed", null);
                    response.add(defaultItem);
                }
            }

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
            Integer vocabId = (Integer) payload.get("vocabId");
            Boolean isCorrect = (Boolean) payload.get("isCorrect");
            Boolean isLearned = (Boolean) payload.get("isLearned");
            Integer memoryLevel = (Integer) payload.get("memoryLevel");

            if (vocabId == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "vocabId is required"));
            }

            if (!vocabularyRepository.existsById(vocabId)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Vocabulary not found"));
            }

            Optional<Progress> existingProgress = progressRepository.findByUserIdAndVocabId(
                    currentUser.getId(), vocabId);

            Progress progress;
            if (existingProgress.isPresent()) {
                progress = existingProgress.get();
            } else {
                progress = new Progress();
                Progress.ProgressId id = new Progress.ProgressId(currentUser.getId(), vocabId);
                progress.setId(id);
                progress.setIsLearned(false);
                progress.setMemoryLevel(1);
                progress.setCorrectStreak(0);
            }

            // Nếu có isCorrect, cập nhật memory và streak
            if (isCorrect != null) {
                int currentLevel = progress.getMemoryLevel();
                int currentStreak = progress.getCorrectStreak() != null ? progress.getCorrectStreak() : 0;
                int newLevel = currentLevel;
                int newStreak = currentStreak;

                if (isCorrect) {
                    newLevel = Math.min(5, currentLevel + 1);
                    newStreak = currentStreak + 1;
                } else {
                    newLevel = Math.max(1, currentLevel - 1);
                    newStreak = 0;
                }

                progress.setMemoryLevel(newLevel);
                progress.setCorrectStreak(newStreak);

                boolean newIsLearned = (newLevel >= 4 && newStreak >= 3);
                progress.setIsLearned(newIsLearned);

            } else {
                if (isLearned != null) progress.setIsLearned(isLearned);
                if (memoryLevel != null) progress.setMemoryLevel(memoryLevel);
            }

            progress.setLastReviewed(LocalDateTime.now());
            progressRepository.save(progress);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Đã cập nhật tiến trình học");
            response.put("vocabId", vocabId);
            response.put("isLearned", progress.getIsLearned());
            response.put("memoryLevel", progress.getMemoryLevel());
            response.put("correctStreak", progress.getCorrectStreak());

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
            Optional<Progress> progressOpt = progressRepository.findByUserIdAndVocabId(
                    currentUser.getId(), vocabId);

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