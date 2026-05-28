package com.example.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "Interactive_Quiz_Sessions")
@Data
public class InteractiveQuizSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "category_id", nullable = false)
    private Integer categoryId;

    @Column(name = "current_scene_id")
    private Integer currentSceneId;

    @Column(name = "current_point_id")
    private Integer currentPointId;

    @Column(name = "answered_points", columnDefinition = "NVARCHAR(MAX)")
    private String answeredPoints;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated;

    @Column(name = "is_completed", nullable = false)
    private Boolean isCompleted = false;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private Users user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", insertable = false, updatable = false)
    private Categories category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_scene_id", insertable = false, updatable = false)
    private InteractiveScene currentScene;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_point_id", insertable = false, updatable = false)
    private InteractivePoint currentPoint;

    @PrePersist
    protected void onCreate() {
        startedAt = LocalDateTime.now();
        lastUpdated = LocalDateTime.now();
        if (answeredPoints == null) {
            answeredPoints = "[]";
        }
    }

    @PreUpdate
    protected void onUpdate() {
        lastUpdated = LocalDateTime.now();
    }

    @Column(name = "answered_questions", columnDefinition = "NVARCHAR(MAX)")
    private String answeredQuestions;
}