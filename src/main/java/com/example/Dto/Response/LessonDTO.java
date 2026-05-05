package com.example.Dto.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LessonDTO {
    private Integer id;
    private String lessonName;
    private Integer orderIndex;
    private Boolean free;
    private Boolean isActive;

}
