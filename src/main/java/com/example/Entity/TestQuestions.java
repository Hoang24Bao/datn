package com.example.Entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "Test_Questions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestQuestions {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "test_id", nullable = false)
    private Integer testId;

    @Column(name = "vocab_id", nullable = false)
    private Integer vocabId;

    @Column(name = "question_text", nullable = false, length = 500)
    private String questionText;

    @Column(name = "correct_answer", nullable = false, length = 255)
    private String correctAnswer;

    @Column(name = "options", nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String options; // JSON array of 4 options

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_id", insertable = false, updatable = false)
    private Tests test;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vocab_id", insertable = false, updatable = false)
    private Vocabulary vocabulary;
}