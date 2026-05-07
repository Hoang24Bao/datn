package com.example.Entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "Question_Categories")
@Data
public class QuestionCategories {

    @EmbeddedId
    private QuestionCategoriesId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("questionId")
    @JoinColumn(name = "question_id")
    private Questions question;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("categoryId")
    @JoinColumn(name = "category_id")
    private Categories category;

    @Embeddable
    @Data
    public static class QuestionCategoriesId implements java.io.Serializable {
        @Column(name = "question_id")
        private Integer questionId;

        @Column(name = "category_id")
        private Integer categoryId;

        public QuestionCategoriesId() {
        }

        public QuestionCategoriesId(Integer questionId, Integer categoryId) {
            this.questionId = questionId;
            this.categoryId = categoryId;
        }
    }
}