package com.example.Dto.Response;

import lombok.Data;

import java.util.List;

@Data
public class SceneQuizDTO {
    private Integer sceneId;
    private String imageUrl;
    private String description;
    private List<PointQuizDTO> points;
}
