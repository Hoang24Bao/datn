package com.example.Dto.Response;

import lombok.Data;

import java.util.List;

@Data
public class QuestionDTO {
    private Integer pointId;
    private String correctExpression;
    private List<String> options; // 4 đáp án (1 đúng + 3 nhiễu)
    private String imageUrl; // ảnh scene để hiển thị
}
