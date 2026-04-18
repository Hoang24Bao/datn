package com.example.Entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "Lesson_Vocab")
@Data
public class LessonVocab {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "lesson_id")
    private Integer lessonId;

    @Column(name = "vocab_id")
    private Integer vocabId;

    @Column(name = "display_order")
    private Integer displayOrder;
}