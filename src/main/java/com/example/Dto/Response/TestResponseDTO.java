package com.example.Dto.Response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TestResponseDTO {
    private Integer id;
    private Integer categoryId;
    private String categoryName;
    private String title;
    private Integer durationMinutes;
    private Integer maxScore;
    private Float passScore;
    private Integer questionCount;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private Boolean hasPassed;
    private Integer bestScore;
}