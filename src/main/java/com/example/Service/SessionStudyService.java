package com.example.Service;

import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@SessionScope
public class SessionStudyService {

    // Snapshot memory gốc từ database (vocabId -> memoryLevel)
    private Map<Integer, Integer> originalMemoryLevels = new ConcurrentHashMap<>();

    // Kết quả tạm thời trong phiên (vocabId -> isLearned: true=đã thuộc, false=chưa thuộc)
    private Map<Integer, Boolean> sessionTempResults = new ConcurrentHashMap<>();

    // Thông tin phiên học
    private Integer currentUserId = null;
    private Integer currentLessonId = null;
    private List<Integer> currentVocabIds = null;

    /**
     * BẮT ĐẦU PHIÊN HỌC
     * Lưu snapshot memory gốc và reset kết quả tạm
     */
    public void startSession(Integer userId, Integer lessonId,
                             List<Integer> vocabIds,
                             Map<Integer, Integer> originalMemoryMap) {
        this.currentUserId = userId;
        this.currentLessonId = lessonId;
        this.currentVocabIds = vocabIds != null ? new ArrayList<>(vocabIds) : null;
        this.originalMemoryLevels.clear();
        this.originalMemoryLevels.putAll(originalMemoryMap);
        this.sessionTempResults.clear();
    }

    /**
     * LƯU KẾT QUẢ TẠM THỜI (ghi đè nếu đã có)
     */
    public void setTempResult(Integer vocabId, Boolean isLearned) {
        if (vocabId != null && isLearned != null) {
            sessionTempResults.put(vocabId, isLearned);
        }
    }

    /**
     * Lấy kết quả tạm của 1 từ
     */
    public Boolean getTempResult(Integer vocabId) {
        return sessionTempResults.get(vocabId);
    }

    /**
     * Lấy TOÀN BỘ kết quả tạm
     */
    public Map<Integer, Boolean> getAllTempResults() {
        return new ConcurrentHashMap<>(sessionTempResults);
    }

    /**
     * Lấy TOÀN BỘ snapshot memory gốc
     */
    public Map<Integer, Integer> getOriginalMemoryLevels() {
        return new ConcurrentHashMap<>(originalMemoryLevels);
    }

    /**
     * Lấy memory gốc của 1 từ
     */
    public Integer getOriginalMemoryLevel(Integer vocabId) {
        return originalMemoryLevels.get(vocabId);
    }

    /**
     * Kiểm tra xem 1 từ đã được đánh giá trong phiên này chưa
     */
    public boolean hasVocabBeenReviewed(Integer vocabId) {
        return sessionTempResults.containsKey(vocabId);
    }

    /**
     * Lấy danh sách vocab đã được đánh giá
     */
    public List<Integer> getReviewedVocabIds() {
        return new ArrayList<>(sessionTempResults.keySet());
    }

    /**
     * Lấy danh sách vocab CHƯA được đánh giá
     */
    public List<Integer> getUnreviewedVocabIds() {
        if (currentVocabIds == null) return new ArrayList<>();

        return currentVocabIds.stream()
                .filter(vocabId -> !sessionTempResults.containsKey(vocabId))
                .collect(Collectors.toList());
    }

    /**
     * KẾT THÚC PHIÊN HỌC - Xóa toàn bộ dữ liệu tạm
     */
    public void endSession() {
        this.originalMemoryLevels.clear();
        this.sessionTempResults.clear();
        this.currentUserId = null;
        this.currentLessonId = null;
        this.currentVocabIds = null;
    }

    /**
     * KIỂM TRA CÓ ĐANG TRONG PHIÊN HỌC KHÔNG
     */
    public boolean isInSession() {
        return currentUserId != null && currentLessonId != null;
    }

    /**
     * Lấy thông tin phiên hiện tại
     */
    public Integer getCurrentUserId() {
        return currentUserId;
    }

    public Integer getCurrentLessonId() {
        return currentLessonId;
    }

    public List<Integer> getCurrentVocabIds() {
        return currentVocabIds != null ? new ArrayList<>(currentVocabIds) : null;
    }
}