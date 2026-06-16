package com.example.Dto.Request;

import lombok.Data;

@Data
public class CreateTestDTO {
    private Integer categoryId;
    private String title;
    private Integer durationMinutes;
    private Integer maxScore;
    private Float passScore;
    private Integer questionCount;
    private String questionType;
    private Boolean isActive;
    private Boolean regenerateQuestions;
}