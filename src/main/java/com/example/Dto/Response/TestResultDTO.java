package com.example.Dto.Response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TestResultDTO {
    private Integer testId;
    private String testTitle;
    private Integer score;
    private Integer maxScore;
    private Integer passScore;
    private Integer correctCount;
    private Integer totalCount;
    private Integer durationSeconds;
    private Boolean isPassed;
    private Integer pointsEarned;
    private Boolean isNewBest;
    private LocalDateTime completedAt;
}