package com.example.Entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "Lessons")
@Data
public class Lessons {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String lessonName;
    private Integer categoryId;
    private String thumbnailUrl;
    private Integer orderIndex;

    @Column(name = "is_free")
    private Boolean free;
}