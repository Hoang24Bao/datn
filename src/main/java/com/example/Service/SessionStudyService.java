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

    private Map<Integer, Integer> originalMemoryLevels = new ConcurrentHashMap<>();

    private Map<Integer, Boolean> sessionTempResults = new ConcurrentHashMap<>();

    private Integer currentUserId = null;
    private Integer currentLessonId = null;
    private List<Integer> currentVocabIds = null;


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


    public void setTempResult(Integer vocabId, Boolean isLearned) {
        if (vocabId != null && isLearned != null) {
            sessionTempResults.put(vocabId, isLearned);
        }
    }


    public Boolean getTempResult(Integer vocabId) {
        return sessionTempResults.get(vocabId);
    }

    public Map<Integer, Boolean> getAllTempResults() {
        return new ConcurrentHashMap<>(sessionTempResults);
    }


    public Map<Integer, Integer> getOriginalMemoryLevels() {
        return new ConcurrentHashMap<>(originalMemoryLevels);
    }


    public Integer getOriginalMemoryLevel(Integer vocabId) {
        return originalMemoryLevels.get(vocabId);
    }

    public boolean hasVocabBeenReviewed(Integer vocabId) {
        return sessionTempResults.containsKey(vocabId);
    }


    public List<Integer> getReviewedVocabIds() {
        return new ArrayList<>(sessionTempResults.keySet());
    }


    public List<Integer> getUnreviewedVocabIds() {
        if (currentVocabIds == null) return new ArrayList<>();

        return currentVocabIds.stream()
                .filter(vocabId -> !sessionTempResults.containsKey(vocabId))
                .collect(Collectors.toList());
    }


    public void endSession() {
        this.originalMemoryLevels.clear();
        this.sessionTempResults.clear();
        this.currentUserId = null;
        this.currentLessonId = null;
        this.currentVocabIds = null;
    }


    public boolean isInSession() {
        return currentUserId != null && currentLessonId != null;
    }


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