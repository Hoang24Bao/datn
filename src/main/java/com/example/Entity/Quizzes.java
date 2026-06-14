package com.example.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "Quizzes")
@Data
public class Quizzes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "type", nullable = false, length = 20)
    private String type;

    @Column(name = "question_count")
    private Integer questionCount = 10;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "quiz", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<QuizCategories> quizCategories;

    @OneToMany(mappedBy = "quiz", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<QuizQuestions> quizQuestions;
}