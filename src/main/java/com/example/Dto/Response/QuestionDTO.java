package com.example.Dto.Response;

import lombok.Data;

import java.util.List;

@Data
public class QuestionDTO {
    private Integer pointId;
    private String correctExpression;
    private List<String> options;
    private String imageUrl;
}
