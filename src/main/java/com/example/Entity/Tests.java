package com.example.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "Tests")
@Data
public class Tests {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Categories category;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @Column(name = "pass_score")
    private Float passScore = 70f;

    @Column(name = "order_index")
    private Integer orderIndex = 0;

    @Column(name = "question_count")
    private Integer questionCount;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}