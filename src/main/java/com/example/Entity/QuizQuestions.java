package com.example.Entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "Quiz_Questions")
@Data
public class QuizQuestions {

    @EmbeddedId
    private QuizQuestionsId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("quizId")
    @JoinColumn(name = "quiz_id")
    private Quizzes quiz;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("questionId")
    @JoinColumn(name = "question_id")
    private Questions question;

    @Embeddable
    @Data
    public static class QuizQuestionsId implements java.io.Serializable {
        @Column(name = "quiz_id")
        private Integer quizId;

        @Column(name = "question_id")
        private Integer questionId;

        public QuizQuestionsId() {
        }

        public QuizQuestionsId(Integer quizId, Integer questionId) {
            this.quizId = quizId;
            this.questionId = questionId;
        }
    }
}