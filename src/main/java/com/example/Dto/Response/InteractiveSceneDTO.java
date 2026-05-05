package com.example.Dto.Response;

import lombok.Data;

@Data
public class InteractiveSceneDTO {
    private Integer id;
    private String imageUrl;
    private String description;
    private Integer orderIndex;
    private Integer pointsCount;

    public InteractiveSceneDTO(Integer id, String imageUrl, String description, Integer orderIndex, Integer pointsCount) {
        this.id = id;
        this.imageUrl = imageUrl;
        this.description = description;
        this.orderIndex = orderIndex;
        this.pointsCount = pointsCount;
    }
}
