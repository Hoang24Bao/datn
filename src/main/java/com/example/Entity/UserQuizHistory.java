package com.example.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "User_Quiz_History")
@Data
public class UserQuizHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quizzes quiz;

    @Column(name = "score")
    private Float score;

    @Column(name = "completed_at")
    private LocalDateTime completedAt = LocalDateTime.now();
}