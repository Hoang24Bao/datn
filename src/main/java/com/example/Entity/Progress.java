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

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private Users user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("vocabId")
    @JoinColumn(name = "vocab_id")
    private Vocabulary vocab;

    @Column(name = "is_learned")
    private Boolean isLearned = false;

    @Column(name = "memory_level")
    private Integer memoryLevel = 1;

    @Column(name = "last_reviewed")
    private LocalDateTime lastReviewed;

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