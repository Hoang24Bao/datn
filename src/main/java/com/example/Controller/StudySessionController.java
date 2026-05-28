package com.example.Controller;

import com.example.Entity.Lessons;
import com.example.Entity.Progress;
import com.example.Entity.Users;
import com.example.Entity.Vocabulary;
import com.example.Repository.LessonsRepository;
import com.example.Repository.ProgressRepository;
import com.example.Repository.UsersRepository;
import com.example.Repository.VocabularyRepository;
import com.example.Service.SessionStudyService;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/study-session")
public class StudySessionController {

    @Autowired
    private SessionStudyService sessionService;

    @Autowired
    private ProgressRepository progressRepository;

    @Autowired
    private VocabularyRepository vocabularyRepository;

    @Autowired
    private LessonsRepository lessonRepository;

    @Autowired
    private UsersRepository userRepository;

    @Autowired
    private EntityManager entityManager;  // ← THAY THẾ LessonVocabRepository

    // ==================== API 1: BẮT ĐẦU PHIÊN HỌC ====================
    @PostMapping("/start")
    public ResponseEntity<?> startSession(@RequestBody Map<String, Object> payload) {
        // TODO: Lấy user hiện tại từ SecurityContext
        Users currentUser = getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Chưa đăng nhập"));
        }

        Integer lessonId = (Integer) payload.get("lessonId");
        if (lessonId == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "lessonId là bắt buộc"));
        }

        // Lấy danh sách vocab_id trong bài học - DÙNG NATIVE QUERY
        @SuppressWarnings("unchecked")
        List<Integer> vocabIds = entityManager.createNativeQuery(
                        "SELECT vocab_id FROM Lesson_Vocab WHERE lesson_id = :lessonId ORDER BY display_order ASC"
                )
                .setParameter("lessonId", lessonId)
                .getResultList();

        if (vocabIds == null || vocabIds.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Bài học không có từ vựng nào"));
        }

        // Lấy snapshot memory gốc từ database
        List<Progress> existingProgress = progressRepository.findByUserIdAndVocabIdIn(
                currentUser.getId(), vocabIds
        );

        Map<Integer, Integer> originalMemoryMap = new HashMap<>();
        for (Integer vocabId : vocabIds) {
            Optional<Progress> prog = existingProgress.stream()
                    .filter(p -> p.getId().getVocabId().equals(vocabId))
                    .findFirst();

            if (prog.isPresent()) {
                originalMemoryMap.put(vocabId, prog.get().getMemoryLevel());
            } else {
                originalMemoryMap.put(vocabId, 1); // Mặc định level 1 cho từ mới
            }
        }

        // Khởi tạo phiên học
        sessionService.startSession(currentUser.getId(), lessonId, vocabIds, originalMemoryMap);

        // Lấy thông tin chi tiết các từ vựng để trả về frontend
        List<Vocabulary> vocabularies = vocabularyRepository.findAllById(vocabIds);

        // Sắp xếp theo đúng thứ tự trong bài học
        Map<Integer, Vocabulary> vocabMap = vocabularies.stream()
                .collect(Collectors.toMap(Vocabulary::getId, v -> v));

        List<Map<String, Object>> vocabList = new ArrayList<>();
        for (Integer vocabId : vocabIds) {
            Vocabulary v = vocabMap.get(vocabId);
            if (v != null) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", v.getId());
                item.put("expression", v.getExpression());
                item.put("kana", v.getKana());
                item.put("romaji", v.getRomaji());
                item.put("meaning", v.getMeaning());
                item.put("imageUrl", v.getImageUrl());
                item.put("audioUrl", v.getAudioUrl());
                item.put("example", v.getExample());
                item.put("exampleVi", v.getExampleVi());
                item.put("originalMemory", originalMemoryMap.get(vocabId));
                vocabList.add(item);
            }
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Bắt đầu phiên học thành công",
                "lessonId", lessonId,
                "totalVocabs", vocabIds.size(),
                "vocabularies", vocabList,
                "originalMemoryLevels", originalMemoryMap
        ));
    }

    // ==================== API 2: LƯU KẾT QUẢ TẠM ====================
    @PostMapping("/temp-result")
    public ResponseEntity<?> saveTempResult(@RequestBody Map<String, Object> payload) {
        Users currentUser = getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Chưa đăng nhập"));
        }

        if (!sessionService.isInSession()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Không có phiên học đang active"));
        }

        Integer vocabId = (Integer) payload.get("vocabId");
        Boolean isLearned = (Boolean) payload.get("isLearned"); // true = đã thuộc, false = chưa thuộc

        if (vocabId == null || isLearned == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Thiếu thông tin vocabId hoặc isLearned"));
        }

        // Chỉ lưu vào bộ nhớ tạm, KHÔNG update database
        sessionService.setTempResult(vocabId, isLearned);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Đã lưu kết quả tạm",
                "vocabId", vocabId,
                "tempResult", isLearned,
                "reviewedCount", sessionService.getReviewedVocabIds().size(),
                "totalCount", sessionService.getCurrentVocabIds().size()
        ));
    }

    // ==================== API 3: KẾT THÚC PHIÊN HỌC ====================
    @PostMapping("/end")
    public ResponseEntity<?> endSession() {
        Users currentUser = getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Chưa đăng nhập"));
        }

        if (!sessionService.isInSession()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Không có phiên học đang active"));
        }

        Map<Integer, Boolean> tempResults = sessionService.getAllTempResults();
        Map<Integer, Integer> originalMemory = sessionService.getOriginalMemoryLevels();

        if (tempResults.isEmpty()) {
            sessionService.endSession();
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Không có từ nào được đánh giá, kết thúc phiên mà không cập nhật",
                    "totalUpdated", 0
            ));
        }

        List<Progress> updates = new ArrayList<>();
        Map<String, Object> resultDetails = new HashMap<>();
        LocalDate today = LocalDate.now();

        for (Map.Entry<Integer, Boolean> entry : tempResults.entrySet()) {
            Integer vocabId = entry.getKey();
            Boolean isCorrect = entry.getValue();

            Optional<Progress> existingProgress = progressRepository.findByUserIdAndVocabId(
                    currentUser.getId(), vocabId
            );

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

            int originalLevel = originalMemory.getOrDefault(vocabId, 1);

            int newLevel;
            if (isCorrect) {
                newLevel = Math.min(5, originalLevel + 1);
            } else {
                newLevel = Math.max(1, originalLevel - 1);
            }

            int currentStreak = progress.getCorrectStreak() != null ? progress.getCorrectStreak() : 0;
            LocalDate lastReviewDate = progress.getLastReviewed() != null ?
                    progress.getLastReviewed().toLocalDate() : null;

            int newStreak;
            if (isCorrect) {
                if (lastReviewDate != null) {
                    long daysDiff = ChronoUnit.DAYS.between(lastReviewDate, today);
                    if (daysDiff == 1) {
                        newStreak = currentStreak + 1;
                    } else if (daysDiff == 0) {
                        newStreak = currentStreak;
                    } else {
                        newStreak = 1;
                    }
                } else {
                    newStreak = 1;
                }
            } else {
                newStreak = 0;
            }

            boolean newIsLearned = (newLevel >= 4 && newStreak >= 3);

            progress.setMemoryLevel(newLevel);
            progress.setCorrectStreak(newStreak);
            progress.setIsLearned(newIsLearned);
            progress.setLastReviewed(LocalDateTime.now());

            updates.add(progress);

            // SỬA ĐOẠN NÀY - dùng HashMap thay vì Map.of()
            Map<String, Object> vocabDetail = new HashMap<>();
            vocabDetail.put("originalLevel", originalLevel);
            vocabDetail.put("newLevel", newLevel);
            vocabDetail.put("oldStreak", currentStreak);
            vocabDetail.put("newStreak", newStreak);
            vocabDetail.put("isLearned", newIsLearned);
            vocabDetail.put("isCorrect", isCorrect);
            if (lastReviewDate != null) {
                vocabDetail.put("daysSinceLastReview", ChronoUnit.DAYS.between(lastReviewDate, today));
            } else {
                vocabDetail.put("daysSinceLastReview", null);
            }

            resultDetails.put("vocab_" + vocabId, vocabDetail);
        }

        progressRepository.saveAll(updates);

        Integer lessonId = sessionService.getCurrentLessonId();
        Lessons lesson = lessonId != null ? lessonRepository.findById(lessonId).orElse(null) : null;

        sessionService.endSession();

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Đã kết thúc phiên học và cập nhật tiến trình");
        response.put("lessonName", lesson != null ? lesson.getLessonName() : null);
        response.put("totalUpdated", updates.size());
        response.put("details", resultDetails);

        return ResponseEntity.ok(response);
    }

    // ==================== API 4: HỦY PHIÊN (KHÔNG LƯU) ====================
    @PostMapping("/cancel")
    public ResponseEntity<?> cancelSession() {
        Users currentUser = getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Chưa đăng nhập"));
        }

        if (!sessionService.isInSession()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Không có phiên học đang active"));
        }

        // Xóa toàn bộ dữ liệu tạm mà không lưu
        sessionService.endSession();

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Đã hủy phiên học, không có thay đổi nào được lưu"
        ));
    }

    // ==================== API 5: LẤY TRẠNG THÁI PHIÊN HIỆN TẠI ====================
    @GetMapping("/status")
    public ResponseEntity<?> getSessionStatus() {
        Users currentUser = getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Chưa đăng nhập"));
        }

        if (!sessionService.isInSession()) {
            return ResponseEntity.ok(Map.of(
                    "inSession", false,
                    "message", "Không có phiên học nào đang active"
            ));
        }

        List<Integer> reviewed = sessionService.getReviewedVocabIds();
        List<Integer> unreviewed = sessionService.getUnreviewedVocabIds();
        Map<Integer, Boolean> tempResults = sessionService.getAllTempResults();

        return ResponseEntity.ok(Map.of(
                "inSession", true,
                "lessonId", sessionService.getCurrentLessonId(),
                "totalVocabs", sessionService.getCurrentVocabIds().size(),
                "reviewedCount", reviewed.size(),
                "unreviewedCount", unreviewed.size(),
                "reviewedVocabIds", reviewed,
                "unreviewedVocabIds", unreviewed,
                "tempResults", tempResults
        ));
    }

    private Users getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof Users) {
            return (Users) principal;
        }

        if (principal instanceof String username) {
            return userRepository.findByUserName(username).orElse(null);
        }

        return null;
    }
}