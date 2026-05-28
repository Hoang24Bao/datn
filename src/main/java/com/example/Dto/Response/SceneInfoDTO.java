package com.example.Dto.Response;

import lombok.Data;

import java.util.List;

@Data
public class SceneInfoDTO {
    private Integer id;
    private String imageUrl;
    private String description;
    private List<PointInfoDTO> points;
}
