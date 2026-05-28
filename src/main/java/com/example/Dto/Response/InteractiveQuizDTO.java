package com.example.Dto.Response;

import lombok.Data;

import java.util.List;

@Data
public class InteractiveQuizDTO {
    private Integer sessionId;
    private Integer categoryId;
    private String categoryName;
    private List<SceneQuizDTO> scenes;
    private Integer currentSceneIndex;
    private Integer currentPointIndex;
    private Integer totalPoints;
    private Integer answeredCount;
}
