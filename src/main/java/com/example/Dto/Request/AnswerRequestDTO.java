package com.example.Dto.Request;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class AnswerRequestDTO {
    private Integer sessionId;
    private Integer pointId;
    private String selectedAnswer;
    private Integer userId;
    private List<Map<String, String>> options;
}
