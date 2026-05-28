package com.example.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "Progress")
@Data
public class Progress {

    @EmbeddedId
    private ProgressId id;

    @Column(name = "is_learned")
    private Boolean isLearned = false;

    @Column(name = "memory_level")
    private Integer memoryLevel = 1;

    @Column(name = "correct_streak")
    private Integer correctStreak = 0;

    @Column(name = "last_reviewed")
    private LocalDateTime lastReviewed;

    @PrePersist
    protected void onCreate() {
        if (isLearned == null) isLearned = false;
        if (memoryLevel == null) memoryLevel = 1;
        lastReviewed = LocalDateTime.now();
    }

    @Embeddable
    @Data
    public static class ProgressId implements Serializable {
        @Column(name = "user_id")
        private Integer userId;

        @Column(name = "vocab_id")
        private Integer vocabId;

        public ProgressId() {
        }

        public ProgressId(Integer userId, Integer vocabId) {
            this.userId = userId;
            this.vocabId = vocabId;
        }
    }
}