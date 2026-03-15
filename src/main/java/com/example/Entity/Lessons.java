package com.example.Entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "Lessons")
@Data
public class Lessons {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String lessonName;

    // Đây là cột khóa ngoại để liên kết với Categories
    private Integer categoryId;

    private String thumbnailUrl;
    private Integer orderIndex;
    private Boolean isFree;
}