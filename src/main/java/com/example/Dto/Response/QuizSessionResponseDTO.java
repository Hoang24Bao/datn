package com.example.Dto.Response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class QuizSessionResponseDTO {
    private Integer id;
    private Integer userId;
    private Integer categoryId;
    private Integer currentSceneId;
    private Integer currentPointId;
    private String answeredPoints;
    private LocalDateTime startedAt;
    private LocalDateTime lastUpdated;
    private Boolean isCompleted;
    private LocalDateTime completedAt;
}