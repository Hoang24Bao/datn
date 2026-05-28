package com.example.Dto.Response;

import lombok.Data;

@Data
public class PointInfoDTO {
    private Integer id;
    private Integer vocabId;
    private String vocabExpression;
    private Double coordX;
    private Double coordY;
    private Double width;
    private Double height;
    private Boolean isAnswered;
}
