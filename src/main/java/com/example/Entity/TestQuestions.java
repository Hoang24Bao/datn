package com.example.Entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "Test_Questions")
@Data
public class TestQuestions {

    @EmbeddedId
    private TestQuestionsId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("testId")
    @JoinColumn(name = "test_id")
    private Tests test;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("questionId")
    @JoinColumn(name = "question_id")
    private Questions question;

    @Column(name = "order_index")
    private Integer orderIndex = 0;

    @Embeddable
    @Data
    public static class TestQuestionsId implements java.io.Serializable {
        @Column(name = "test_id")
        private Integer testId;

        @Column(name = "question_id")
        private Integer questionId;

        public TestQuestionsId() {
        }

        public TestQuestionsId(Integer testId, Integer questionId) {
            this.testId = testId;
            this.questionId = questionId;
        }
    }
}