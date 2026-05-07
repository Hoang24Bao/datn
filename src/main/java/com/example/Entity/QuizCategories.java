package com.example.Entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "Quiz_Categories")
@Data
public class QuizCategories {

    @EmbeddedId
    private QuizCategoriesId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("quizId")
    @JoinColumn(name = "quiz_id")
    private Quizzes quiz;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("categoryId")
    @JoinColumn(name = "category_id")
    private Categories category;

    @Embeddable
    @Data
    public static class QuizCategoriesId implements java.io.Serializable {
        @Column(name = "quiz_id")
        private Integer quizId;

        @Column(name = "category_id")
        private Integer categoryId;

        public QuizCategoriesId() {
        }

        public QuizCategoriesId(Integer quizId, Integer categoryId) {
            this.quizId = quizId;
            this.categoryId = categoryId;
        }
    }
}