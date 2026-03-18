package com.example.Entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "Categories")
@Data
public class Categories {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String categoryName;
    private String slug;
    private String iconUrl;
    private Integer totalLessons;
    private Integer progress;
    private String jlptLevel;
}