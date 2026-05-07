package com.example.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "User_Test_Results")
@Data
public class UserTestResults {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_id", nullable = false)
    private Tests test;

    @Column(name = "score", nullable = false)
    private Float score;

    @Column(name = "correct_count", nullable = false)
    private Integer correctCount;

    @Column(name = "total_count", nullable = false)
    private Integer totalCount;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    @Column(name = "completed_at")
    private LocalDateTime completedAt = LocalDateTime.now();
}